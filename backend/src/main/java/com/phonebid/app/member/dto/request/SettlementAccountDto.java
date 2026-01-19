package com.phonebid.app.member.dto.request;

import com.phonebid.app.mypage.domain.Bank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정산 계좌 DTO
 * 판매자 정산 계좌 정보를 담는 DTO 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementAccountDto {

    @NotBlank(message = "은행을 선택해주세요.")
    private String bankName;

    @NotBlank(message = "계좌번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    @Size(min = 10, max = 20, message = "계좌번호는 10자 이상 20자 이하여야 합니다.")
    private String accountNumber;

    @NotBlank(message = "예금주명을 입력해주세요.")
    @Size(min = 2, max = 50, message = "예금주명은 2자 이상 50자 이하여야 합니다.")
    private String accountHolderName;

    public Bank getBank() {
        return Bank.fromDisplayName(bankName);
    }
}

