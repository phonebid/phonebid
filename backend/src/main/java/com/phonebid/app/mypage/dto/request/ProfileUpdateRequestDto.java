package com.phonebid.app.mypage.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequestDto {

    @Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
    private String name;

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$", message = "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다")
    private String nickname;

    @Pattern(regexp = "^[0-9]+$", message = "휴대전화번호는 숫자만 입력 가능합니다")
    @Size(min = 10, max = 11, message = "휴대전화번호는 10자리 또는 11자리여야 합니다")
    private String phone;
}

