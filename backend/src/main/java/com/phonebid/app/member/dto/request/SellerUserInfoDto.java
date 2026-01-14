package com.phonebid.app.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매자 회원가입용 사용자 정보 DTO
 * 판매자 회원가입 시 사용하는 사용자 정보 (이메일은 SellerRegisterRequestDto에서 관리)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerUserInfoDto {

    @NotBlank(message = "유저ID는 필수입니다")
    @Size(min = 4, max = 10, message = "유저ID는 4자 이상 10자 이하여야 합니다")
    @Pattern(regexp = "^[a-z0-9]+$", message = "유저ID는 알파벳 소문자와 숫자로만 구성되어야 합니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$", message = "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다")
    private String nickname;
}

