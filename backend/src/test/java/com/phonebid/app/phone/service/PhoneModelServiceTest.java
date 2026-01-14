package com.phonebid.app.phone.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.PhoneErrorCode;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneModelImage;
import com.phonebid.app.phone.domain.PhoneOption.OptionType;
import com.phonebid.app.phone.dto.request.PhoneModelCreateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneModelCreateRequestDto.OptionItem;
import com.phonebid.app.phone.dto.request.PhoneModelUpdateRequestDto;
import com.phonebid.app.phone.dto.response.PhoneModelResponseDto;
import com.phonebid.app.phone.repository.PhoneModelRepository;
import com.phonebid.app.phone.repository.PhoneModelImageRepository;
import com.phonebid.app.phone.repository.PhoneOptionRepository;
import com.phonebid.app.s3.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhoneModelService 테스트")
class PhoneModelServiceTest {

    @Mock
    private PhoneModelRepository phoneModelRepository;

    @Mock
    private PhoneOptionRepository phoneOptionRepository;

    @Mock
    private PhoneModelImageRepository phoneModelImageRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PhoneModelService phoneModelService;

    private PhoneModelCreateRequestDto validCreateRequest;
    private PhoneModelUpdateRequestDto validUpdateRequest;
    private PhoneModel savedModel;
    private UUID modelId;

    @BeforeEach
    void setUp() {
        modelId = UUID.randomUUID();

        // 유효한 모델 생성 요청
        validCreateRequest = new PhoneModelCreateRequestDto(
            Brand.APPLE,
            "iPhone 16",
            "A3101",
            1350000,
            LocalDate.of(2024, 9, 20),
            null
        );

        // 저장된 모델
        savedModel = PhoneModel.builder()
            .brand(Brand.APPLE)
            .model("iPhone 16")
            .modelNumber("A3101")
            .releasedPrice(1350000)
            .releasedAt(LocalDate.of(2024, 9, 20))
            .build();

        // 업데이트 요청
        validUpdateRequest = new PhoneModelUpdateRequestDto(
            Brand.SAMSUNG,
            "Galaxy S24",
            "SM-S921",
            1200000,
            LocalDate.of(2024, 1, 18)
        );
    }

    @Test
    @DisplayName("모델 생성 - 성공")
    void createPhoneModel_Success() {
        // given
        when(phoneModelRepository.findByBrandAndModel(Brand.APPLE, "iPhone 16"))
            .thenReturn(Optional.empty());
        when(phoneModelRepository.save(any(PhoneModel.class)))
            .thenReturn(savedModel);

        // when
        PhoneModelResponseDto result = phoneModelService.createPhoneModel(validCreateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBrand()).isEqualTo(Brand.APPLE);
        assertThat(result.getModel()).isEqualTo("iPhone 16");
        verify(phoneModelRepository, times(1)).save(any(PhoneModel.class));
    }

    @Test
    @DisplayName("모델 생성 - 중복 실패")
    void createPhoneModel_DuplicateFail() {
        // given
        when(phoneModelRepository.findByBrandAndModel(Brand.APPLE, "iPhone 16"))
            .thenReturn(Optional.of(savedModel));

        // when & then
        assertThatThrownBy(() -> phoneModelService.createPhoneModel(validCreateRequest))
            .isInstanceOf(CustomException.class);
        
        verify(phoneModelRepository, never()).save(any(PhoneModel.class));
    }

    @Test
    @DisplayName("모델 생성 + 옵션 동시 생성 - 옵션이 있는 경우")
    void createPhoneModelWithOptions_Success() {
        // given
        List<OptionItem> options = Arrays.asList(
            new OptionItem(OptionType.COLOR, "Black", "블랙")
        );
        
        PhoneModelCreateRequestDto requestWithOptions = new PhoneModelCreateRequestDto(
            Brand.APPLE,
            "iPhone 16",
            "A3101",
            1350000,
            LocalDate.of(2024, 9, 20),
            options
        );

        when(phoneModelRepository.findByBrandAndModel(Brand.APPLE, "iPhone 16"))
            .thenReturn(Optional.empty());
        when(phoneModelRepository.save(any(PhoneModel.class)))
            .thenReturn(savedModel);
        lenient().when(phoneOptionRepository.existsByModelIdAndOptionTypeAndOptionValue(any(), any(), anyString()))
            .thenReturn(false);

        // when
        PhoneModelResponseDto result = phoneModelService.createPhoneModel(requestWithOptions);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getModel()).isEqualTo("iPhone 16");
        verify(phoneModelRepository, times(1)).save(any(PhoneModel.class));
        // 옵션 저장이 호출되었는지 확인
        verify(phoneOptionRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("모델 목록 조회 - 성공")
    void getPhoneModels_Success() {
        // given
        List<PhoneModel> models = Arrays.asList(savedModel);
        when(phoneModelRepository.findAll()).thenReturn(models);
        when(phoneModelImageRepository.findByPhoneModelIdInOrderByDisplayOrder(anyList()))
            .thenReturn(Collections.emptyList());

        // when
        List<PhoneModelResponseDto> result = phoneModelService.getPhoneModels();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("iPhone 16");
        verify(phoneModelRepository, times(1)).findAll();
        verify(phoneModelImageRepository, times(1)).findByPhoneModelIdInOrderByDisplayOrder(anyList());
    }

    @Test
    @DisplayName("모델 목록 조회 - 빈 목록 실패")
    void getPhoneModels_EmptyFail() {
        // given
        when(phoneModelRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> phoneModelService.getPhoneModels())
            .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("모델 단건 조회 - 성공")
    void getPhoneModel_Success() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(modelId))
            .thenReturn(Collections.emptyList());

        // when
        PhoneModelResponseDto result = phoneModelService.getPhoneModel(modelId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getModel()).isEqualTo("iPhone 16");
        verify(phoneModelRepository, times(1)).findById(modelId);
        verify(phoneModelImageRepository, times(1)).findByPhoneModelIdOrderByDisplayOrder(modelId);
    }

    @Test
    @DisplayName("모델 단건 조회 - 존재하지 않음 실패")
    void getPhoneModel_NotFoundFail() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> phoneModelService.getPhoneModel(modelId))
            .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("모델 수정 - 성공")
    void updatePhoneModel_Success() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneModelRepository.findByBrandAndModel(Brand.SAMSUNG, "Galaxy S24"))
            .thenReturn(Optional.empty());
        when(phoneModelRepository.save(any(PhoneModel.class)))
            .thenReturn(savedModel);

        // when
        PhoneModelResponseDto result = phoneModelService.updatePhoneModel(modelId, validUpdateRequest);

        // then
        assertThat(result).isNotNull();
        verify(phoneModelRepository, times(1)).save(savedModel);
    }

    @Test
    @DisplayName("모델 수정 - 존재하지 않음 실패")
    void updatePhoneModel_NotFoundFail() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> phoneModelService.updatePhoneModel(modelId, validUpdateRequest))
            .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("모델 삭제 - 이미지가 있는 경우 성공")
    void deletePhoneModel_WithImages_Success() {
        // given
        String imageUrl1 = "https://s3.amazonaws.com/bucket/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/bucket/image2.jpg";
        
        PhoneModelImage image1 = PhoneModelImage.builder()
            .phoneModel(savedModel)
            .imageUrl(imageUrl1)
            .displayOrder(1)
            .build();
        
        PhoneModelImage image2 = PhoneModelImage.builder()
            .phoneModel(savedModel)
            .imageUrl(imageUrl2)
            .displayOrder(2)
            .build();
        
        List<PhoneModelImage> images = Arrays.asList(image1, image2);
        
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(modelId))
            .thenReturn(images);
        doNothing().when(s3Service).deleteFileByUrl(anyString());

        // when
        phoneModelService.deletePhoneModel(modelId);

        // then
        verify(phoneModelRepository, times(1)).findById(modelId);
        verify(phoneModelImageRepository, times(1)).findByPhoneModelIdOrderByDisplayOrder(modelId);
        verify(s3Service, times(1)).deleteFileByUrl(imageUrl1);
        verify(s3Service, times(1)).deleteFileByUrl(imageUrl2);
        verify(phoneModelImageRepository, times(1)).deleteAll(images);
        verify(phoneModelRepository, times(1)).delete(savedModel);
    }

    @Test
    @DisplayName("모델 삭제 - 이미지가 없는 경우 성공")
    void deletePhoneModel_NoImages_Success() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(modelId))
            .thenReturn(Collections.emptyList());

        // when
        phoneModelService.deletePhoneModel(modelId);

        // then
        verify(phoneModelRepository, times(1)).findById(modelId);
        verify(phoneModelImageRepository, times(1)).findByPhoneModelIdOrderByDisplayOrder(modelId);
        verify(s3Service, never()).deleteFileByUrl(anyString());
        verify(phoneModelImageRepository, never()).deleteAll(anyList());
        verify(phoneModelRepository, times(1)).delete(savedModel);
    }

    @Test
    @DisplayName("모델 삭제 - 모델을 찾을 수 없음 실패")
    void deletePhoneModel_NotFoundFail() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> phoneModelService.deletePhoneModel(modelId))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException customException = (CustomException) exception;
                assertThat(customException.getErrorCode()).isEqualTo(PhoneErrorCode.PHONE_MODEL_NOT_FOUND);
            });
        
        verify(phoneModelRepository, times(1)).findById(modelId);
        verify(phoneModelImageRepository, never()).findByPhoneModelIdOrderByDisplayOrder(any(UUID.class));
        verify(s3Service, never()).deleteFileByUrl(anyString());
        verify(phoneModelImageRepository, never()).deleteAll(anyList());
        verify(phoneModelRepository, never()).delete(any(PhoneModel.class));
    }

    @Test
    @DisplayName("모델 삭제 - S3 삭제 실패")
    void deletePhoneModel_S3DeleteFail() {
        // given
        String imageUrl = "https://s3.amazonaws.com/bucket/image1.jpg";
        
        PhoneModelImage image = PhoneModelImage.builder()
            .phoneModel(savedModel)
            .imageUrl(imageUrl)
            .displayOrder(1)
            .build();
        
        List<PhoneModelImage> images = Arrays.asList(image);
        
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(modelId))
            .thenReturn(images);
        doThrow(new CustomException(PhoneErrorCode.IMAGE_DELETE_FAILED))
            .when(s3Service).deleteFileByUrl(imageUrl);

        // when & then
        assertThatThrownBy(() -> phoneModelService.deletePhoneModel(modelId))
            .isInstanceOf(CustomException.class)
            .satisfies(exception -> {
                CustomException customException = (CustomException) exception;
                assertThat(customException.getErrorCode()).isEqualTo(PhoneErrorCode.IMAGE_DELETE_FAILED);
            });
        
        verify(phoneModelRepository, times(1)).findById(modelId);
        verify(phoneModelImageRepository, times(1)).findByPhoneModelIdOrderByDisplayOrder(modelId);
        verify(s3Service, times(1)).deleteFileByUrl(imageUrl);
        verify(phoneModelImageRepository, never()).deleteAll(anyList());
        verify(phoneModelRepository, never()).delete(any(PhoneModel.class));
    }
}
