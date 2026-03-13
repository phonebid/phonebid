package com.phonebid.app.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IdentityVerificationRequestDto {

    @NotBlank(message = "본인인증 ID는 필수입니다.")
    private String identityVerificationId;
}
