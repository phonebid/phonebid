package com.phonebid.app.mypage.dto.request;

import com.phonebid.app.mypage.domain.Bank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountCreateRequestDto {

    @NotBlank(message = "은행명을 입력해주세요.")
    private String bankName;

    @NotBlank(message = "계좌번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9-]+$", message = "계좌번호는 숫자와 하이픈(-)만 입력 가능합니다.")
    @Size(min = 10, max = 20, message = "계좌번호는 10자 이상 20자 이하여야 합니다.")
    private String accountNumber;

    @NotBlank(message = "예금주명을 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$", message = "예금주명은 한글, 영문, 숫자, 공백만 입력 가능합니다.")
    @Size(min = 2, max = 50, message = "예금주명은 2자 이상 50자 이하여야 합니다.")
    private String accountHolderName;

    public Bank getBank() {
        return Bank.fromDisplayName(bankName);
    }
}

