package com.phonebid.app.phone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 휴대폰 모델 삭제 요청 DTO
 */
@Getter
@NoArgsConstructor
public class PhoneModelDeleteRequestDto {

    @NotNull(message = "모델 ID는 필수입니다.")
    private UUID id;

    public PhoneModelDeleteRequestDto(UUID id) {
        this.id = id;
    }
}
