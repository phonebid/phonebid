package com.phonebid.app.auction.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.hibernate.annotations.Comment;

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
    @Comment("입찰 수정 이력 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @Column(name = "version", nullable = false)
    @Comment("수정 버전")
    private Integer version;

    @Column(name = "price", nullable = false)
    @Comment("입찰가")
    private Integer price;

    @Builder
    public BidHistory(Bid bid, Integer version, Integer price) {
        this.bid = bid;
        this.version = version;
        this.price = price;
    }

    // 정적 팩토리 메서드
    public static BidHistory createFromBid(Bid bid, Integer version) {
        return BidHistory.builder()
                .bid(bid)
                .version(version)
                .price(bid.getPrice())
                .build();
    }

    // 비즈니스 메서드
    public boolean isLatestVersion(Integer currentVersion) {
        return this.version.equals(currentVersion);
    }

    public String getHistorySummary() {
        return String.format("v%d: %,d원", version, price);
    }

    public boolean hasPriceChanged(Integer newPrice) {
        return !this.price.equals(newPrice);
    }


} 