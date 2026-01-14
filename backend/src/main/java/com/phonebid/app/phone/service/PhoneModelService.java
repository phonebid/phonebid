package com.phonebid.app.phone.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.phonebid.app.phone.repository.PhoneModelRepository;
import com.phonebid.app.phone.repository.PhoneModelImageRepository;
import com.phonebid.app.phone.dto.request.PhoneModelCreateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneModelCreateRequestDto.OptionItem;
import com.phonebid.app.phone.dto.request.PhoneModelUpdateRequestDto;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.PhoneErrorCode;
import com.phonebid.app.phone.dto.response.PhoneModelResponseDto;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.domain.PhoneModelImage;
import com.phonebid.app.phone.repository.PhoneOptionRepository;
import com.phonebid.app.s3.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneModelService {
    
    private final PhoneModelRepository phoneModelRepository;
    private final PhoneOptionRepository phoneOptionRepository;
    private final PhoneModelImageRepository phoneModelImageRepository;
    private final S3Service s3Service;

    /**
     * 휴대폰 모델 목록 조회
     * 각 모델의 썸네일 이미지 URL을 함께 조회하여 반환
     * 성능 최적화: N+1 쿼리 문제를 방지하기 위해 모든 모델의 이미지를 한 번에 조회
     */
    public List<PhoneModelResponseDto> getPhoneModels() {
        List<PhoneModel> models = phoneModelRepository.findAll();
        if (models.isEmpty()) {
            throw new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND);
        }

        // 모든 모델 ID 수집
        List<UUID> modelIds = models.stream()
            .map(PhoneModel::getId)
            .collect(Collectors.toList());

        // 각 모델의 첫 번째 이미지 URL을 한 번에 조회 (N+1 쿼리 방지)
        Map<UUID, String> thumbnailImageMap = phoneModelImageRepository
            .findByPhoneModelIdInOrderByDisplayOrder(modelIds)
            .stream()
            .collect(Collectors.toMap(
                image -> image.getPhoneModel().getId(),
                PhoneModelImage::getImageUrl,
                (existing, replacement) -> existing // 첫 번째 이미지만 유지
            ));

        // 각 모델에 썸네일 이미지 URL 포함하여 DTO 생성
        return models.stream()
            .map(model -> {
                String thumbnailUrl = thumbnailImageMap.get(model.getId());
                return PhoneModelResponseDto.from(model, thumbnailUrl);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public PhoneModelResponseDto createPhoneModel(PhoneModelCreateRequestDto requestDto) {
        if (phoneModelRepository.findByBrandAndModel(requestDto.getBrand(), requestDto.getModel()).isPresent()) {
            throw new CustomException(PhoneErrorCode.PHONE_MODEL_ALREADY_EXISTS);
        }
        // 1) 모델 저장
        PhoneModel savedModel = phoneModelRepository.save(requestDto.toEntity());

        // 2) 옵션이 있으면 검증 후 벌크 저장
        if (requestDto.hasOptions()) {
            // 요청 내 중복 제거 및 유효성 검사
            validateAndSaveOptions(requestDto.getOptions(), savedModel);
        }

        return PhoneModelResponseDto.from(savedModel);
    }
    
    @Transactional
    public PhoneModelResponseDto updatePhoneModel(UUID id, PhoneModelUpdateRequestDto requestDto) {
        PhoneModel existingModel = phoneModelRepository.findById(id)
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));
        
        // 브랜드와 모델명 조합의 중복 확인 (둘 다 업데이트되는 경우)
        validateBrandModelCombination(requestDto, existingModel);
        
        // 한 번의 메서드 호출로 모든 필드 업데이트
        updateFields(requestDto, existingModel);
        return PhoneModelResponseDto.from(phoneModelRepository.save(existingModel));
    }

    private void validateAndSaveOptions(List<OptionItem> options, PhoneModel savedModel) {
        // 요청 내부 중복 제거를 위한 키: type + value
        Set<String> dedupe = new HashSet<>();
        List<PhoneOption> entities = new ArrayList<>();

        for (OptionItem item : options) {
            if (item.getType() == null || item.getValue() == null || item.getValue().trim().isEmpty()) {
                throw new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND); // 유효성 에러 전용 코드가 없으므로 재사용
            }
            String key = item.getType().name() + "|" + item.getValue().trim();
            if (!dedupe.add(key)) {
                // 요청 내 중복
                throw new CustomException(PhoneErrorCode.PHONE_OPTION_ALREADY_EXISTS);
            }

            entities.add(PhoneOption.builder()
                .model(savedModel)
                .optionType(item.getType())
                .optionValue(item.getValue().trim())
                .displayLabel(item.getDisplayLabel())
                .build());
        }

        if (!entities.isEmpty()) {
            phoneOptionRepository.saveAll(entities);
        }
    }

    private void validateBrandModelCombination(PhoneModelUpdateRequestDto requestDto, PhoneModel existingModel) {
        Brand brandToCheck = requestDto.hasBrand() ? requestDto.getBrand() : existingModel.getBrand();
        String modelToCheck = requestDto.hasModel() ? requestDto.getModel() : existingModel.getModel();
        
        // 브랜드나 모델명이 변경되는 경우에만 중복 확인
        if (requestDto.hasBrand() || requestDto.hasModel()) {
            phoneModelRepository.findByBrandAndModel(brandToCheck, modelToCheck)
                .filter(model -> !model.getId().equals(existingModel.getId()))
                .ifPresent(model -> {
                    throw new CustomException(PhoneErrorCode.PHONE_MODEL_ALREADY_EXISTS);
                });
        }
    }

    private void updateFields(PhoneModelUpdateRequestDto requestDto, PhoneModel existingModel) {
        // 한 번의 메서드 호출로 모든 필드 업데이트
        existingModel.updateAll(requestDto.getBrand(), requestDto.getModel(), requestDto.getModelNumber(), requestDto.getReleasedPrice(), requestDto.getReleasedAt());
    }

    
    /**
     * 휴대폰 모델 삭제
     * 해당 모델의 모든 이미지를 S3에서 삭제한 후 DB에서도 삭제합니다.
     */
    @Transactional
    public void deletePhoneModel(UUID id) {
        PhoneModel model = phoneModelRepository.findById(id)
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));
        
        // 해당 모델의 모든 이미지 조회
        List<PhoneModelImage> images = phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(id);
        
        // S3에서 이미지 파일 삭제
        for (PhoneModelImage image : images) {
            try {
                String imageUrl = image.getImageUrl();
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    s3Service.deleteFileByUrl(imageUrl);
                    log.info("휴대폰 모델 이미지 S3 삭제 완료: modelId={}, imageUrl={}", id, imageUrl);
                }
            } catch (CustomException e) {
                // S3Service에서 발생한 CustomException의 원인 예외를 로그에 기록
                Throwable cause = e.getCause();
                if (cause != null) {
                    log.error("휴대폰 모델 이미지 S3 삭제 실패: modelId={}, imageUrl={}", id, image.getImageUrl(), cause);
                } else {
                    log.error("휴대폰 모델 이미지 S3 삭제 실패: modelId={}, imageUrl={}, errorCode={}, message={}", 
                            id, image.getImageUrl(), e.getErrorCode(), e.getMessage(), e);
                }
                throw new CustomException(PhoneErrorCode.IMAGE_DELETE_FAILED);
            }
        }
        
        // DB에서 이미지 삭제
        if (!images.isEmpty()) {
            phoneModelImageRepository.deleteAll(images);
        }
        
        // DB에서 모델 삭제
        phoneModelRepository.delete(model);
        log.info("휴대폰 모델 삭제 완료: modelId={}, modelName={}", id, model.getFullModelName());
    }
    
    public PhoneModelResponseDto getPhoneModel(UUID id) {
        PhoneModel model = phoneModelRepository.findById(id)
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));
        
        // 첫 번째 이미지 URL 조회
        List<PhoneModelImage> images = phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(id);
        String thumbnailUrl = images.isEmpty() ? null : images.get(0).getImageUrl();
        
        return PhoneModelResponseDto.from(model, thumbnailUrl);
    }
}
