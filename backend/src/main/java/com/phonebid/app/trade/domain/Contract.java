package com.phonebid.app.trade.domain;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.Quote;
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
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contracts_quote_id", columnList = "quote_id"),
    @Index(name = "idx_contracts_bid_id", columnList = "bid_id"),
    @Index(name = "idx_contracts_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("계약 고유 ID (UUID)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false, unique = true)
    private Quote quote;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false, unique = true)
    private Bid selectedBid;



    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("계약 상태 (SIGNING, SIGNED, CANCELLED)")
    private ContractStatus status;

    @Column(name = "signed_at")
    @Comment("계약 체결 일시")
    private LocalDateTime signedAt;

    @Builder
    public Contract(Quote quote, Bid selectedBid) {
        validateContractCreation(quote, selectedBid);
        
        this.quote = quote;
        this.selectedBid = selectedBid;
        this.status = ContractStatus.SIGNING; // 기본값: 서명 대기
    }

    // 비즈니스 메서드
    public void sign() {
        if (!status.canSign()) {
            throw new CustomException(TradeErrorCode.CONTRACT_CANNOT_SIGN);
        }
        
        this.status = ContractStatus.SIGNED;
        this.signedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new CustomException(TradeErrorCode.CONTRACT_CANNOT_CANCEL);
        }
        
        this.status = ContractStatus.CANCELLED;
    }

    public boolean isCompleted() {
        return status.isSigned();
    }

    public boolean isPending() {
        return status.isSigning();
    }

    public Integer getContractAmount() {
        return selectedBid.getPrice();
    }

    public String getContractSummary() {
        return String.format("계약금액: %,d원, 상태: %s", 
            getContractAmount(), status.getDisplayName());
    }

    // 검증 메서드
    private void validateContractCreation(Quote quote, Bid selectedBid) {
        if (!quote.canSelectBid()) {
            throw new CustomException(TradeErrorCode.INVALID_BID_FOR_QUOTE);
        }
        
        if (!selectedBid.getQuote().equals(quote)) {
            throw new CustomException(TradeErrorCode.INVALID_BID_FOR_QUOTE);
        }
        
        // 입찰이 ACTIVE 상태인지 확인 (계약 생성 시 자동으로 SELECTED로 변경됨)
        if (!selectedBid.isActive()) {
            throw new CustomException(TradeErrorCode.BID_NOT_ACTIVE);
        }
    }
}