package com.phonebid.app.phone.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.PhoneErrorCode;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.domain.PhoneOption.OptionType;
import com.phonebid.app.phone.dto.request.PhoneOptionCreateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneOptionUpdateRequestDto;
import com.phonebid.app.phone.dto.response.PhoneOptionResponseDto;
import com.phonebid.app.phone.repository.PhoneModelRepository;
import com.phonebid.app.phone.repository.PhoneOptionRepository;
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
@DisplayName("PhoneOptionService 테스트")
class PhoneOptionServiceTest {

    @Mock
    private PhoneOptionRepository phoneOptionRepository;

    @Mock
    private PhoneModelRepository phoneModelRepository;

    @InjectMocks
    private PhoneOptionService phoneOptionService;

    private PhoneModel savedModel;
    private PhoneOption savedOption;
    private PhoneOptionCreateRequestDto validCreateRequest;
    private PhoneOptionUpdateRequestDto validUpdateRequest;
    private UUID modelId;
    private UUID optionId;

    @BeforeEach
    void setUp() {
        modelId = UUID.randomUUID();
        optionId = UUID.randomUUID();

        // 저장된 모델
        savedModel = PhoneModel.builder()
            .brand(Brand.APPLE)
            .model("iPhone 16")
            .modelNumber("A3101")
            .releasedPrice(1350000)
            .releasedAt(LocalDate.of(2024, 9, 20))
            .build();

        // 저장된 옵션
        savedOption = PhoneOption.builder()
            .model(savedModel)
            .optionType(OptionType.COLOR)
            .optionValue("Black")
            .displayLabel("블랙")
            .build();

        // 유효한 옵션 생성 요청
        validCreateRequest = new PhoneOptionCreateRequestDto(
            modelId,
            OptionType.COLOR,
            "White",
            "화이트"
        );

        // 업데이트 요청
        validUpdateRequest = new PhoneOptionUpdateRequestDto(
            optionId,
            OptionType.STORAGE,
            "256",
            "256GB"
        );
    }

    @Test
    @DisplayName("옵션 생성 - 성공")
    void createPhoneOption_Success() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(
            modelId, OptionType.COLOR, "White"))
            .thenReturn(Optional.empty());
        when(phoneOptionRepository.save(any(PhoneOption.class)))
            .thenReturn(savedOption);

        // when
        PhoneOptionResponseDto result = phoneOptionService.createPhoneOption(validCreateRequest);

        // then
        assertThat(result).isNotNull();
        verify(phoneModelRepository, times(1)).findById(modelId);
        verify(phoneOptionRepository, times(1)).save(any(PhoneOption.class));
    }

    @Test
    @DisplayName("옵션 생성 - 모델 존재하지 않음 실패")
    void createPhoneOption_ModelNotFoundFail() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> phoneOptionService.createPhoneOption(validCreateRequest))
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_MODEL_NOT_FOUND.getMessage());
        
        verify(phoneOptionRepository, never()).save(any(PhoneOption.class));
    }

    @Test
    @DisplayName("옵션 생성 - 중복 실패")
    void createPhoneOption_DuplicateFail() {
        // given
        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(
            modelId, OptionType.COLOR, "White"))
            .thenReturn(Optional.of(savedOption));

        // when & then
        assertThatThrownBy(() -> phoneOptionService.createPhoneOption(validCreateRequest))
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_OPTION_ALREADY_EXISTS.getMessage());
        
        verify(phoneOptionRepository, never()).save(any(PhoneOption.class));
    }

    @Test
    @DisplayName("옵션 여러 개 생성 - 성공")
    void createPhoneOptions_Success() {
        // given
        List<PhoneOptionCreateRequestDto> requests = Arrays.asList(
            new PhoneOptionCreateRequestDto(modelId, OptionType.COLOR, "Black", "블랙"),
            new PhoneOptionCreateRequestDto(modelId, OptionType.COLOR, "White", "화이트"),
            new PhoneOptionCreateRequestDto(modelId, OptionType.STORAGE, "128", "128GB")
        );

        when(phoneModelRepository.findById(modelId))
            .thenReturn(Optional.of(savedModel));
        when(phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(phoneOptionRepository.save(any(PhoneOption.class)))
            .thenReturn(savedOption);

        // when
        List<PhoneOptionResponseDto> result = phoneOptionService.createPhoneOptions(requests);

        // then
        assertThat(result).hasSize(3);
        verify(phoneOptionRepository, times(3)).save(any(PhoneOption.class));
    }

    @Test
    @DisplayName("옵션 여러 개 생성 - 빈 리스트 실패")
    void createPhoneOptions_EmptyListFail() {
        // given
        List<PhoneOptionCreateRequestDto> emptyList = Collections.emptyList();

        // when & then
        assertThatThrownBy(() -> phoneOptionService.createPhoneOptions(emptyList))
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_OPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("옵션 목록 조회 - 성공")
    void getPhoneOptions_Success() {
        // given
        List<PhoneOption> options = Arrays.asList(savedOption);
        when(phoneOptionRepository.findAll()).thenReturn(options);

        // when
        List<PhoneOptionResponseDto> result = phoneOptionService.getPhoneOptions();

        // then
        assertThat(result).hasSize(1);
        verify(phoneOptionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("옵션 목록 조회 - 빈 목록 실패")
    void getPhoneOptions_EmptyFail() {
        // given
        when(phoneOptionRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> phoneOptionService.getPhoneOptions())
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_OPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("옵션 단건 조회 - 성공")
    void getPhoneOption_Success() {
        // given
        when(phoneOptionRepository.findById(optionId))
            .thenReturn(Optional.of(savedOption));

        // when
        PhoneOptionResponseDto result = phoneOptionService.getPhoneOption(optionId);

        // then
        assertThat(result).isNotNull();
        verify(phoneOptionRepository, times(1)).findById(optionId);
    }

    @Test
    @DisplayName("옵션 단건 조회 - 존재하지 않음 실패")
    void getPhoneOption_NotFoundFail() {
        // given
        when(phoneOptionRepository.findById(optionId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> phoneOptionService.getPhoneOption(optionId))
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_OPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("옵션 수정 - 성공")
    void updatePhoneOption_Success() {
        // given
        when(phoneOptionRepository.findById(optionId))
            .thenReturn(Optional.of(savedOption));
        when(phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(
            any(), eq(OptionType.STORAGE), eq("256")))
            .thenReturn(Optional.empty());
        when(phoneOptionRepository.save(any(PhoneOption.class)))
            .thenReturn(savedOption);

        // when
        PhoneOptionResponseDto result = phoneOptionService.updatePhoneOption(validUpdateRequest);

        // then
        assertThat(result).isNotNull();
        verify(phoneOptionRepository, times(1)).save(savedOption);
    }

    @Test
    @DisplayName("옵션 수정 - 존재하지 않음 실패")
    void updatePhoneOption_NotFoundFail() {
        // given
        when(phoneOptionRepository.findById(optionId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> phoneOptionService.updatePhoneOption(validUpdateRequest))
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_OPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("옵션 수정 - 중복 실패")
    void updatePhoneOption_DuplicateFail() {
        // given
        PhoneOption anotherOption = PhoneOption.builder()
            .model(savedModel)
            .optionType(OptionType.STORAGE)
            .optionValue("256")
            .displayLabel("256GB")
            .build();

        when(phoneOptionRepository.findById(optionId))
            .thenReturn(Optional.of(savedOption));
        when(phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(
            any(), eq(OptionType.STORAGE), eq("256")))
            .thenReturn(Optional.of(anotherOption));

        // when & then
        assertThatThrownBy(() -> phoneOptionService.updatePhoneOption(validUpdateRequest))
            .isInstanceOf(CustomException.class)
            .hasMessage(PhoneErrorCode.PHONE_OPTION_ALREADY_EXISTS.getMessage());
    }
}

