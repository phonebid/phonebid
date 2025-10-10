package com.phonebid.app.phone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 휴대폰 옵션 삭제 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PhoneOptionDeleteRequestDto {

    @NotNull(message = "옵션 ID는 필수입니다.")
    private UUID id;

    public PhoneOptionDeleteRequestDto(UUID id) {
        this.id = id;
    }
}
