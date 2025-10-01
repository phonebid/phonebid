package com.phonebid.app.phone.dto.response;

import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * 휴대폰 모델 응답 DTO
 */
@Getter
@NoArgsConstructor
public class PhoneModelResponseDto {

    private UUID id;
    private Brand brand;
    
    private String model;
    private String modelNumber;
    private Integer releasedPrice;
    private LocalDate releasedAt;
    private List<PhoneOptionResponseDto> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 옵션 정보는 별도 API로 조회하거나 PhoneModelWithOptionsResponseDto 사용

    public PhoneModelResponseDto(UUID id, Brand brand, String model, String modelNumber, 
                               Integer releasedPrice, LocalDate releasedAt, List<PhoneOptionResponseDto> options,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.modelNumber = modelNumber;
        this.releasedPrice = releasedPrice;
        this.releasedAt = releasedAt;
        this.options = options;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    /**
     * Entity를 DTO로 변환하는 정적 메서드
     */
    public static PhoneModelResponseDto from(PhoneModel phoneModel) {
        List<PhoneOptionResponseDto> optionDtos = null;
        if (phoneModel.getOptions() != null) {
            optionDtos = phoneModel.getOptions().stream()
                .map(PhoneOptionResponseDto::from)
                .collect(Collectors.toList());
        }
        
        return new PhoneModelResponseDto(
            phoneModel.getId(),
            phoneModel.getBrand(),
            phoneModel.getModel(),
            phoneModel.getModelNumber(),
            phoneModel.getReleasedPrice(),
            phoneModel.getReleasedAt(),
            optionDtos,
            phoneModel.getCreatedAt(),
            phoneModel.getUpdatedAt()
        );
    }

}
