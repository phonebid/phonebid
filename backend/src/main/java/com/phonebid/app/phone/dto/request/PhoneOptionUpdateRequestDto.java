package com.phonebid.app.phone.dto.request;

import com.phonebid.app.phone.domain.PhoneOption.OptionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 휴대폰 옵션 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PhoneOptionUpdateRequestDto {

    @NotNull(message = "옵션 ID는 필수입니다.")
    private UUID id;

    private OptionType optionType;

    private String optionValue;

    private String displayLabel;

    public PhoneOptionUpdateRequestDto(UUID id, OptionType optionType, 
                                     String optionValue, String displayLabel) {
        this.id = id;
        this.optionType = optionType;
        this.optionValue = optionValue;
        this.displayLabel = displayLabel;
    }
}
