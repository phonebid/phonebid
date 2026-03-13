package com.phonebid.app.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentityVerificationResponseDto {
    private boolean verified;
    private String name;
    private String phone;
    private String carrier;
}
