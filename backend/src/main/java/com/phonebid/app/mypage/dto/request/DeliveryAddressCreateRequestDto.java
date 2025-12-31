package com.phonebid.app.mypage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryAddressCreateRequestDto {

    @NotBlank(message = "배송지명을 입력해주세요.")
    @Size(min = 1, max = 50, message = "배송지명은 1자 이상 50자 이하여야 합니다.")
    private String addressName;

    @NotBlank(message = "받는사람을 입력해주세요.")
    @Size(min = 1, max = 50, message = "받는사람은 1자 이상 50자 이하여야 합니다.")
    private String recipientName;

    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^[0-9-+()\\s]+$", message = "연락처는 숫자와 하이픈(-), 괄호(), 공백만 입력 가능합니다.")
    @Size(min = 10, max = 20, message = "연락처는 10자 이상 20자 이하여야 합니다.")
    private String phone;

    @NotBlank(message = "우편번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String postalCode;

    @NotBlank(message = "주소를 입력해주세요.")
    @Size(min = 1, max = 200, message = "주소는 1자 이상 200자 이하여야 합니다.")
    private String address;

    @Size(max = 200, message = "상세주소는 200자 이하여야 합니다.")
    private String detailAddress;

    private Boolean saveAsDefault;
}

