package com.phonebid.app.phone.dto.response;

import com.phonebid.app.phone.domain.Brand;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 휴대폰 모델과 옵션을 함께 포함하는 응답 DTO
 * 견적 등록 시 사용자에게 선택 옵션을 제공할 때 사용
 */
@Getter
@NoArgsConstructor
public class PhoneModelWithOptionsResponseDto {

    private UUID id;
    private Brand brand;
    private String brandDisplayName;
    private String model;
    private String modelNumber;
    private Integer releasedPrice;
    private LocalDate releasedAt;
    private String fullModelName;
    private String modelSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 옵션 정보
    private List<PhoneOptionResponseDto> allOptions;
    private Map<String, List<PhoneOptionResponseDto>> optionsByType;
    private List<PhoneOptionResponseDto> colorOptions;
    private List<PhoneOptionResponseDto> storageOptions;
    private int totalOptionCount;

    public PhoneModelWithOptionsResponseDto(UUID id, Brand brand, String model, String modelNumber, 
                                          Integer releasedPrice, LocalDate releasedAt, String fullModelName, 
                                          String modelSummary, LocalDateTime createdAt, LocalDateTime updatedAt,
                                          List<PhoneOptionResponseDto> allOptions) {
        this.id = id;
        this.brand = brand;
        this.brandDisplayName = brand != null ? brand.getDisplayName() : null;
        this.model = model;
        this.modelNumber = modelNumber;
        this.releasedPrice = releasedPrice;
        this.releasedAt = releasedAt;
        this.fullModelName = fullModelName;
        this.modelSummary = modelSummary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.allOptions = allOptions;
        this.totalOptionCount = allOptions != null ? allOptions.size() : 0;
        
        // 옵션을 타입별로 분류
        if (allOptions != null) {
            this.optionsByType = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getOptionType().name()));
            
            this.colorOptions = allOptions.stream()
                .filter(PhoneOptionResponseDto::isColorOption)
                .collect(Collectors.toList());
            
            this.storageOptions = allOptions.stream()
                .filter(PhoneOptionResponseDto::isStorageOption)
                .collect(Collectors.toList());
        }
    }

    /**
     * 특정 타입의 옵션만 가져오기
     */
    public List<PhoneOptionResponseDto> getOptionsByType(String optionType) {
        return optionsByType != null ? optionsByType.get(optionType) : List.of();
    }

    /**
     * 색상 옵션이 있는지 확인
     */
    public boolean hasColorOptions() {
        return colorOptions != null && !colorOptions.isEmpty();
    }

    /**
     * 저장용량 옵션이 있는지 확인
     */
    public boolean hasStorageOptions() {
        return storageOptions != null && !storageOptions.isEmpty();
    }

    /**
     * 옵션이 있는지 확인
     */
    public boolean hasOptions() {
        return totalOptionCount > 0;
    }
}
