package com.phonebid.app.trade.domain;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false, unique = true)
    private Quote quote;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false, unique = true)
    private Bid selectedBid;



    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status;

    @Column(name = "signed_at")
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
            throw new IllegalStateException("서명할 수 없는 계약 상태입니다: " + status.getDisplayName());
        }
        
        this.status = ContractStatus.SIGNED;
        this.signedAt = LocalDateTime.now();
        
        // 관련 엔터티 상태 변경
        this.quote.contract();
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 계약 상태입니다: " + status.getDisplayName());
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
            throw new IllegalStateException("입찰 선택이 불가능한 견적입니다.");
        }
        
        if (!selectedBid.getQuote().equals(quote)) {
            throw new IllegalArgumentException("선택된 입찰이 해당 견적에 속하지 않습니다.");
        }
        

    }
}