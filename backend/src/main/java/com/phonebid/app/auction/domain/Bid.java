package com.phonebid.app.auction.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "bids", indexes = {
    @Index(name = "idx_bids_quote_id", columnList = "quote_id"),
    @Index(name = "idx_bids_seller_id", columnList = "seller_id"),
    @Index(name = "idx_bids_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    @Column(name = "rating_snapshot")
    private Double ratingSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BidStatus status;

    @Builder
    public Bid(Quote quote, Seller seller, Integer price, Integer deliveryDays, Double ratingSnapshot) {
        this.quote = quote;
        this.seller = seller;
        this.price = price;
        this.deliveryDays = deliveryDays;
        this.ratingSnapshot = ratingSnapshot;
        this.status = BidStatus.ACTIVE; // 기본값: 활성
    }

    // 비즈니스 메서드
    public boolean canModify() {
        return status.canModify() && quote.canReceiveBids();
    }

    public void updateBid(Integer newPrice, Integer newDeliveryDays) {
        if (!canModify()) {
            throw new IllegalStateException("수정할 수 없는 입찰입니다.");
        }
        this.price = newPrice;
        this.deliveryDays = newDeliveryDays;
    }

    public void select() {
        if (!status.isActive()) {
            throw new IllegalStateException("활성 상태가 아닌 입찰은 선택할 수 없습니다.");
        }
        this.status = BidStatus.SELECTED;
    }

    public void cancel() {
        if (!status.isActive()) {
            throw new IllegalStateException("활성 상태가 아닌 입찰은 취소할 수 없습니다.");
        }
        this.status = BidStatus.CANCELLED;
    }

    public boolean isWinningBid() {
        return status.isSelected();
    }

    public String getBidSummary() {
        return String.format("입찰가: %,d원, 배송예정: %d일", price, deliveryDays);
    }
} 