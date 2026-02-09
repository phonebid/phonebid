package com.phonebid.app.auction.domain;


import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
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
    @JoinColumn(name = "storage", nullable = true, columnDefinition = "UUID")
    private PhoneOption storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color", nullable = true, columnDefinition = "UUID")
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
        String storageStr = storage != null ? storage.getDisplayLabel() : "상관없음";
        String colorStr = color != null ? color.getDisplayLabel() : "상관없음";
        return String.format("%s %s %s %s", phoneModel.getFullModelName(), storageStr, carrier.getDisplayName(), colorStr);
    }

    /**
     * 견적 종료 (CLOSED 상태로 변경)
     * 내부에서 종료 가능 여부를 검증합니다.
     * 
     * @throws CustomException 이미 종료되었거나 계약 완료된 견적인 경우 예외 발생
     */
    public void close() {
        validateCanBeClosed();
        this.status = QuoteStatus.CLOSED;
    }

    /**
     * 견적 계약 완료 (CONTRACTED 상태로 변경)
     * 입찰 선택 및 계약 생성 시 호출됩니다.
     * 현재 상태가 OPEN인 경우에만 CONTRACTED로 변경 가능합니다.
     * 
     * @throws CustomException 현재 상태가 OPEN이 아닌 경우, AuctionErrorCode.INVALID_QUOTE_STATUS 예외 발생
     */
    public void markContracted() {
        if (!status.isOpen()) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }
        this.status = QuoteStatus.CONTRACTED;
    }

    /**
     * 삭제된 견적인지 검증
     * 
     * @throws CustomException 삭제된 견적인 경우, AuctionErrorCode.QUOTE_NOT_FOUND 예외 발생
     */
    public void validateNotDeleted() {
        if (this.isDelete != null && this.isDelete) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND);
        }
    }

    /**
     * 견적 소유자 검증
     * 
     * @param userId 검증할 사용자 ID
     * @throws CustomException 소유자가 아닌 경우, AuctionErrorCode.QUOTE_NOT_OWNED_BY_USER 예외 발생
     */
    public void validateOwnership(UUID userId) {
        if (!this.user.getId().equals(userId)) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_OWNED_BY_USER);
        }
    }

    /**
     * 종료 가능 여부 검증
     * 
     * @throws CustomException 이미 종료되었거나 계약 완료된 견적인 경우 예외 발생
     */
    public void validateCanBeClosed() {
        if (this.status == QuoteStatus.CLOSED) {
            throw new CustomException(AuctionErrorCode.QUOTE_ALREADY_CLOSED);
        }
        if (this.status == QuoteStatus.CONTRACTED) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }
    }
}
