package com.phonebid.app.mypage.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_accounts_user_id", columnList = "user_id"),
    @Index(name = "idx_accounts_bank", columnList = "bank")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("계좌 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("계좌 소유자")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank", nullable = false)
    @Comment("은행명")
    private Bank bank;

    @Column(name = "account_number", nullable = false)
    @Comment("계좌번호")
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false)
    @Comment("예금주명")
    private String accountHolderName;

    @Builder
    public Account(User user, Bank bank, String accountNumber, String accountHolderName) {
        this.user = user;
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
    }

    public void updateAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void updateAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        int length = accountNumber.length();
        return accountNumber.substring(0, 4) + "-****-" + accountNumber.substring(length - 4);
    }

    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDelete);
    }
}

