package com.phonebid.app.auction.domain;


import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Comment;


@Entity
@Table(name = "quotes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("견적 요청 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_model_id", nullable = false)
    private PhoneModel phoneModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage", nullable = false)
    private PhoneOption storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color", nullable = false)
    private PhoneOption color;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    @Comment("희망 통신사 (SKT, KT, LGU)")
    private Carrier carrier;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("견적 상태 (OPEN, CLOSED, CONTRACTED)")
    private QuoteStatus status;

    @Column(name = "expired_at", nullable = false)
    @Comment("경매 마감 시각")
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_method")
    @Comment("구매방법 (NUMBER_TRANSFER, DEVICE_CHANGE, NEW_SUBSCRIPTION, LOWEST_PRICE, ANY)")
    private PurchaseMethod purchaseMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_carrier")
    @Comment("현재 통신사 (번호이동/기기변경 시)")
    private Carrier currentCarrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_method")
    @Comment("개통방법 (SELECTIVE_SUBSIDY, COMMON_SUBSIDY, ANY)")
    private ActivationMethod activationMethod;

    @Builder
    public Quote(User user, PhoneModel phoneModel, PhoneOption storage,  PhoneOption color, Carrier carrier,
                 LocalDateTime expiredAt, PurchaseMethod purchaseMethod, Carrier currentCarrier,
                 ActivationMethod activationMethod) {
        this.user = user;
        this.phoneModel = phoneModel;
        this.storage = storage;
        this.color = color;
        this.carrier = carrier;
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

    public String getFullSpecification() {
        return String.format("%s %s %s %s", phoneModel, storage, carrier.getDisplayName(), color);
    }
}
