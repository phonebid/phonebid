package com.phonebid.app.phone.dto.response;

import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.domain.PhoneOption.OptionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 휴대폰 옵션 응답 DTO
 */
@Getter
@NoArgsConstructor
public class PhoneOptionResponseDto {

    private UUID id;
    private UUID modelId;
    private OptionType optionType;
    private String optionTypeDisplayName;
    private String optionValue;
    private String displayLabel;
    private String formattedValue;
    private String optionSummary;
    private boolean isColorOption;
    private boolean isStorageOption;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PhoneOptionResponseDto(UUID id, UUID modelId, OptionType optionType, 
                                String optionValue, String displayLabel, String formattedValue, 
                                String optionSummary, boolean isColorOption, boolean isStorageOption,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.modelId = modelId;
        this.optionType = optionType;
        this.optionTypeDisplayName = optionType != null ? optionType.getDisplayName() : null;
        this.optionValue = optionValue;
        this.displayLabel = displayLabel;
        this.formattedValue = formattedValue;
        this.optionSummary = optionSummary;
        this.isColorOption = isColorOption;
        this.isStorageOption = isStorageOption;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Entity를 DTO로 변환하는 정적 메서드
     */
    public static PhoneOptionResponseDto from(PhoneOption phoneOption) {
        return new PhoneOptionResponseDto(
            phoneOption.getId(),
            phoneOption.getModel().getId(),
            phoneOption.getOptionType(),
            phoneOption.getOptionValue(),
            phoneOption.getDisplayLabel(),
            phoneOption.getFormattedValue(),
            phoneOption.getOptionSummary(),
            phoneOption.isColorOption(),
            phoneOption.isStorageOption(),
            phoneOption.getCreatedAt(),
            phoneOption.getUpdatedAt()
        );
    }
}
