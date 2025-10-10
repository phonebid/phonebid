package com.phonebid.app.phone.dto.request;

import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.domain.PhoneOption.OptionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 휴대폰 옵션 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PhoneOptionCreateRequestDto {

    @NotNull(message = "모델 ID는 필수입니다.")
    private UUID modelId;

    @NotNull(message = "옵션 타입은 필수입니다.")
    private OptionType optionType;

    @NotBlank(message = "옵션 값은 필수입니다.")
    private String optionValue;

    private String displayLabel;

    public PhoneOptionCreateRequestDto(UUID modelId, OptionType optionType, 
                                     String optionValue, String displayLabel) {
        this.modelId = modelId;
        this.optionType = optionType;
        this.optionValue = optionValue;
        this.displayLabel = displayLabel;
    }

    public PhoneOption toEntity(PhoneModel model) {
        return PhoneOption.builder()
            .model(model)
            .optionType(optionType)
            .optionValue(optionValue)
            .displayLabel(displayLabel)
            .build();
    }
}
