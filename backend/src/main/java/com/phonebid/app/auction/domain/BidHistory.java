package com.phonebid.app.auction.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "bid_history", indexes = {
    @Index(name = "idx_bid_history_bid_id", columnList = "bid_id"),
    @Index(name = "idx_bid_history_version", columnList = "bid_id, version")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    @Builder
    public BidHistory(Bid bid, Integer version, Integer price, Integer deliveryDays) {
        this.bid = bid;
        this.version = version;
        this.price = price;
        this.deliveryDays = deliveryDays;
    }

    // 정적 팩토리 메서드
    public static BidHistory createFromBid(Bid bid, Integer version) {
        return BidHistory.builder()
                .bid(bid)
                .version(version)
                .price(bid.getPrice())
                .deliveryDays(bid.getDeliveryDays())
                .build();
    }

    // 비즈니스 메서드
    public boolean isLatestVersion(Integer currentVersion) {
        return this.version.equals(currentVersion);
    }

    public String getHistorySummary() {
        return String.format("v%d: %,d원, %d일", version, price, deliveryDays);
    }

    public boolean hasPriceChanged(Integer newPrice) {
        return !this.price.equals(newPrice);
    }

    public boolean hasDeliveryDaysChanged(Integer newDeliveryDays) {
        return !this.deliveryDays.equals(newDeliveryDays);
    }
} 