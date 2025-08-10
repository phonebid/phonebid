package com.phonebid.app.auction.domain;


import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "quotes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "storage", nullable = false)
    private String storage;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    private Carrier carrier;

    @Column(name = "color", nullable = false)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuoteStatus status;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_method")
    private PurchaseMethod purchaseMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_carrier")
    private Carrier currentCarrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_method")
    private ActivationMethod activationMethod;

    @Builder
    public Quote(User user, String model, String storage, Carrier carrier, String color, 
                 LocalDateTime expiredAt, PurchaseMethod purchaseMethod, Carrier currentCarrier, 
                 ActivationMethod activationMethod) {
        this.user = user;
        this.model = model;
        this.storage = storage;
        this.carrier = carrier;
        this.color = color;
        this.purchaseMethod = purchaseMethod;
        this.currentCarrier = currentCarrier;
        this.activationMethod = activationMethod;
        this.status = QuoteStatus.OPEN; // 기본값: 진행중
        this.expiredAt = expiredAt != null ? expiredAt : LocalDateTime.now().plusHours(24); // 기본 24시간
    }

    // 비즈니스 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean canReceiveBids() {
        return status.canReceiveBids() && !isExpired();
    }

    public boolean canSelectBid() {
        return status.canSelectBid() && !isExpired();
    }

    public void close() {
        if (status != QuoteStatus.OPEN) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }
        this.status = QuoteStatus.CLOSED;
    }

    public void contract() {
        if (!canSelectBid()) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }
        this.status = QuoteStatus.CONTRACTED;
    }

    public void extendExpiration(LocalDateTime newExpiredAt) {
        if (status != QuoteStatus.OPEN) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }
        if (newExpiredAt.isBefore(LocalDateTime.now())) {
            throw new CustomException(AuctionErrorCode.INVALID_END_TIME);
        }
        this.expiredAt = newExpiredAt;
    }

    public String getFullSpecification() {
        return String.format("%s %s %s %s", model, storage, carrier.getDisplayName(), color);
    }
}
