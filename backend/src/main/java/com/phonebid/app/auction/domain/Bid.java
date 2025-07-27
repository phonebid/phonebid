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
    @Index(name = "idx_bids_seller_id", columnList = "seller_id")
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
    @Column(name = "purchase_method", nullable = false)
    private PurchaseMethod purchaseMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    private Carrier carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_carrier")
    private Carrier currentCarrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_method", nullable = false)
    private ActivationMethod activationMethod;

    @Column(name = "additional_subsidy")
    private Integer additionalSubsidy;

    @Column(name = "installment_principal")
    private Integer installmentPrincipal;

    @Column(name = "additional_services", length = 500)
    private String additionalServices;

    @Embedded
    private PricePlan pricePlan;

    @Column(name = "contract_months")
    private Integer contractMonths;

    @Builder
    public Bid(Quote quote, Seller seller, Integer price, Integer deliveryDays, Double ratingSnapshot,
               PurchaseMethod purchaseMethod, Carrier carrier, Carrier currentCarrier, ActivationMethod activationMethod,
               Integer additionalSubsidy, Integer installmentPrincipal, String additionalServices,
               PricePlan pricePlan, Integer contractMonths) {
        validateBidCreation(purchaseMethod, activationMethod, currentCarrier);
        
        this.quote = quote;
        this.seller = seller;
        this.price = price;
        this.deliveryDays = deliveryDays;
        this.ratingSnapshot = ratingSnapshot;
        this.purchaseMethod = purchaseMethod;
        this.carrier = carrier;
        this.currentCarrier = currentCarrier;
        this.activationMethod = activationMethod;
        this.additionalSubsidy = additionalSubsidy;
        this.installmentPrincipal = installmentPrincipal;
        this.additionalServices = additionalServices;
        this.pricePlan = pricePlan;
        this.contractMonths = contractMonths;
    }

    // 비즈니스 메서드
    public boolean canModify() {
        return quote.canReceiveBids();
    }

    public void updateBid(Integer newPrice, Integer newDeliveryDays) {
        if (!canModify()) {
            throw new IllegalStateException("수정할 수 없는 입찰입니다.");
        }
        this.price = newPrice;
        this.deliveryDays = newDeliveryDays;
    }

    public String getBidSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("입찰가: %,d원, 배송예정: %d일", price, deliveryDays));
        
        if (pricePlan != null && pricePlan.isComplete()) {
            summary.append(", 요금제: ").append(pricePlan.getPlanSummary());
        }
        
        if (contractMonths != null && contractMonths > 0) {
            summary.append(", 약정: ").append(contractMonths).append("개월");
        }
        
        return summary.toString();
    }

    // 총 비용 계산 (입찰가 + 요금제 + 추가지원금)
    public Integer getTotalCost() {
        Integer total = price;
        
        if (additionalSubsidy != null) {
            total += additionalSubsidy;
        }
        
        if (pricePlan != null && pricePlan.getPlanPrice() != null) {
            // 약정개월 수만큼 요금제 비용 추가
            Integer months = contractMonths != null ? contractMonths : 24; // 기본 24개월
            total += pricePlan.getPlanPrice() * months;
        }
        
        return total;
    }

    // 월 평균 비용 계산
    public Integer getMonthlyAverageCost() {
        Integer totalCost = getTotalCost();
        Integer months = contractMonths != null && contractMonths > 0 ? contractMonths : 24;
        return totalCost / months;
    }

    // 비즈니스 검증 메서드들
    public boolean isNumberTransfer() {
        return purchaseMethod != null && purchaseMethod.isNumberTransfer();
    }

    public boolean requiresCurrentCarrier() {
        return purchaseMethod != null && purchaseMethod.requiresCurrentCarrier();
    }

    public boolean hasContract() {
        return contractMonths != null && contractMonths > 0;
    }

    public boolean hasPricePlan() {
        return pricePlan != null && pricePlan.isComplete();
    }

    // 검증 메서드
    private void validateBidCreation(PurchaseMethod purchaseMethod, ActivationMethod activationMethod, Carrier currentCarrier) {
        if (purchaseMethod == null) {
            throw new IllegalArgumentException("구매방법은 필수입니다.");
        }
        
        if (!purchaseMethod.isValidForBid()) {
            throw new IllegalArgumentException("입찰에서는 '상관없음' 구매방법을 사용할 수 없습니다.");
        }
        
        if (activationMethod == null) {
            throw new IllegalArgumentException("개통방법은 필수입니다.");
        }
        
        if (!activationMethod.isValidForBid()) {
            throw new IllegalArgumentException("입찰에서는 '상관없음' 개통방법을 사용할 수 없습니다.");
        }
        
        // 번호이동이나 기기변경 시 기존 통신사 정보 필요
        if (purchaseMethod.requiresCurrentCarrier() && currentCarrier == null) {
            throw new IllegalArgumentException(
                purchaseMethod.getDisplayName() + " 시에는 기존 통신사 정보가 필요합니다.");
        }
    }
} 