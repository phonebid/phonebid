package com.phonebid.app.mypage.dto.response;

import com.phonebid.app.mypage.domain.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class AccountResponseDto {

    private UUID accountId;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private LocalDateTime createdAt;

    public static AccountResponseDto from(Account account) {
        AccountResponseDto dto = new AccountResponseDto();
        dto.accountId = account.getId();
        dto.bankName = account.getBank().getDisplayName();
        dto.accountNumber = account.getAccountNumber();
        dto.accountHolderName = account.getAccountHolderName();
        dto.createdAt = account.getCreatedAt();
        return dto;
    }
}

