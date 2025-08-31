package com.phonebid.app.trade.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.TradeErrorCode;
import com.phonebid.app.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Comment;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_contract_id", columnList = "contract_id"),
    @Index(name = "idx_payments_pg_tid", columnList = "pg_tid"),
    @Index(name = "idx_payments_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("결제 고유 ID (UUID)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    @Column(name = "amount", nullable = false)
    @Comment("결제 금액")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    @Comment("결제 방법 (CARD, TRANSFER, POINT)")
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("결제 상태 (REQUESTED, PENDING_APPROVAL, PAID, FAILED)")
    private PaymentStatus status;

    @Column(name = "pg_tid", nullable = false, unique = true)
    @Comment("PG 거래 고유 ID")
    private String pgTid;

    @Column(name = "paid_at")
    @Comment("결제 완료 일시")
    private LocalDateTime paidAt;



    @Builder
    public Payment(Contract contract, Integer amount, PaymentMethod method, String pgTid) {
        validatePaymentCreation(contract, amount);
        
        this.contract = contract;
        this.amount = amount;
        this.method = method;
        this.pgTid = pgTid;
        this.status = PaymentStatus.REQUESTED; // 기본값: 결제 요청
    }

    // 비즈니스 메서드
    public void approve() {
        if (!status.isRequested()) {
            throw new CustomException(TradeErrorCode.PAYMENT_CANNOT_APPROVE);
        }
        this.status = PaymentStatus.PENDING_APPROVAL;
    }

    public void complete() {
        if (!status.canComplete()) {
            throw new CustomException(TradeErrorCode.PAYMENT_CANNOT_COMPLETE);
        }
        
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void fail() {
        if (!status.canFail()) {
            throw new CustomException(TradeErrorCode.PAYMENT_CANNOT_FAIL);
        }
        
        this.status = PaymentStatus.FAILED;
    }

    public boolean isSuccessful() {
        return status.isPaid();
    }

    public boolean needsManualConfirmation() {
        return method.requiresManualConfirmation();
    }

    public String getPaymentSummary() {
        return String.format("%s %,d원 (%s)", 
            method.getDisplayName(), amount, status.getDisplayName());
    }

    // 검증 메서드
    private void validatePaymentCreation(Contract contract, Integer amount) {
        if (!contract.isCompleted()) {
            throw new CustomException(TradeErrorCode.INVALID_CONTRACT_STATUS);
        }
        
        if (amount == null || amount <= 0) {
            throw new CustomException(TradeErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        
        if (!amount.equals(contract.getContractAmount())) {
            throw new CustomException(TradeErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}