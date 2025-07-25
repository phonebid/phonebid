package com.phonebid.app.auction.domain;


import com.phonebid.app.common.domain.BaseEntity;
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

    @Builder
    public Quote(User user, String model, String storage, Carrier carrier, String color, LocalDateTime expiredAt) {
        this.user = user;
        this.model = model;
        this.storage = storage;
        this.carrier = carrier;
        this.color = color;
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
            throw new IllegalStateException("진행중인 견적만 마감할 수 있습니다.");
        }
        this.status = QuoteStatus.CLOSED;
    }

    public void contract() {
        if (!canSelectBid()) {
            throw new IllegalStateException("입찰 선택이 불가능한 견적입니다.");
        }
        this.status = QuoteStatus.CONTRACTED;
    }

    public void extendExpiration(LocalDateTime newExpiredAt) {
        if (status != QuoteStatus.OPEN) {
            throw new IllegalStateException("진행중인 견적만 마감 시간을 연장할 수 있습니다.");
        }
        if (newExpiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감 시간은 현재 시간보다 이후여야 합니다.");
        }
        this.expiredAt = newExpiredAt;
    }

    public String getFullSpecification() {
        return String.format("%s %s %s %s", model, storage, carrier.getDisplayName(), color);
    }
}
