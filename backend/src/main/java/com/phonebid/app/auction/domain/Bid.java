package com.phonebid.app.auction.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Comment;

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
    @Comment("입찰 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_plan_id", nullable = false)
    private PricePlan pricePlan;


    @Column(name = "price", nullable = false)
    @Comment("입찰가 (원)")
    private Integer price;

    @Column(name = "delivery_days", nullable = false)
    @Comment("배송 예상일 (일)")
    private Integer deliveryDays;

    @Column(name = "rating_snapshot")
    @Comment("입찰 당시 판매자 평점 스냅샷")
    private Double ratingSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_method", nullable = false)
    @Comment("구매방법 (NUMBER_TRANSFER, DEVICE_CHANGE, NEW_SUBSCRIPTION, ANY)")
    private PurchaseMethod purchaseMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    @Comment("통신사 (이동할/사용할 통신사) 3사 (SKT, KT, LGU)")
    private Carrier carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_carrier")
    @Comment("기존 통신사 (번호이동/기기변경 시)")
    private Carrier currentCarrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_method", nullable = false)
    @Comment("개통방법 (SELECTIVE_SUBSIDY, COMMON_SUBSIDY, ANY)")
    private ActivationMethod activationMethod;

    @Column(name = "additional_subsidy")
    @Comment("추가지원금 (원)")
    private Integer additionalSubsidy;

    @Column(name = "installment_principal")
    @Comment("할부원금 (원)")
    private Integer installmentPrincipal;

    @Column(name = "additional_services", length = 500)
    @Comment("부가서비스 설명")
    private String additionalServices;


    @Column(name = "contract_months")
    @Comment("약정개월")
    private Integer contractMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("입찰 상태 (ACTIVE, SELECTED, CANCELLED)")
    private BidStatus status;

    @OneToMany(mappedBy = "bid", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BidAdditionalService> additionalServiceList = new ArrayList<>();

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
        this.status = BidStatus.ACTIVE;
    }

    public void addAdditionalService(BidAdditionalService additionalService) {
        this.additionalServiceList.add(additionalService);
    }

    public void select() {
        if (this.status != BidStatus.ACTIVE) {
            throw new CustomException(AuctionErrorCode.BID_NOT_ALLOWED);
        }
        this.status = BidStatus.SELECTED;
    }

    public void cancel() {
        if (this.status != BidStatus.ACTIVE) {
            throw new CustomException(AuctionErrorCode.BID_NOT_ALLOWED);
        }
        this.status = BidStatus.CANCELLED;
    }

    public boolean isActive() {
        return this.status == BidStatus.ACTIVE;
    }

    public boolean isSelected() {
        return this.status == BidStatus.SELECTED;
    }

    // 비즈니스 메서드
    public boolean canModify() {
        return isActive() && quote.canReceiveBids();
    }

    public void updateBid(Integer newPrice, Integer newDeliveryDays) {
        if (!canModify()) {
            throw new CustomException(AuctionErrorCode.BID_MODIFICATION_NOT_ALLOWED);
        }
        this.price = newPrice;
        this.deliveryDays = newDeliveryDays;
    }

    /**
     * 입찰 정보 수정 (포괄적)
     */
    public void updateBidDetails(BidUpdateCommand command) {
        if (!canModify()) {
            throw new CustomException(AuctionErrorCode.BID_MODIFICATION_NOT_ALLOWED);
        }
        
        if (command.hasPrice()) {
            this.price = command.getPrice();
        }
        if (command.hasDeliveryDays()) {
            this.deliveryDays = command.getDeliveryDays();
        }
        if (command.hasAdditionalSubsidy()) {
            this.additionalSubsidy = command.getAdditionalSubsidy();
        }
        if (command.hasInstallmentPrincipal()) {
            this.installmentPrincipal = command.getInstallmentPrincipal();
        }
        if (command.hasContractMonths()) {
            this.contractMonths = command.getContractMonths();
        }
    }

    /**
     * 요금제 변경
     */
    public void updatePricePlan(PricePlan newPricePlan) {
        if (!canModify()) {
            throw new CustomException(AuctionErrorCode.BID_MODIFICATION_NOT_ALLOWED);
        }
        this.pricePlan = newPricePlan;
    }

    /**
     * 부가서비스 목록 교체
     */
    public void replaceAdditionalServices(List<BidAdditionalService> newServices) {
        if (!canModify()) {
            throw new CustomException(AuctionErrorCode.BID_MODIFICATION_NOT_ALLOWED);
        }
        this.additionalServiceList.clear();
        if (newServices != null) {
            for (BidAdditionalService service : newServices) {
                if (service.getBid() == null || !service.getBid().getId().equals(this.id)) {
                    throw new CustomException(AuctionErrorCode.BID_NOT_ALLOWED);
                }
            }
            this.additionalServiceList.addAll(newServices);
        }
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
            throw new CustomException(AuctionErrorCode.INVALID_PURCHASE_METHOD);
        }
        
        if (!purchaseMethod.isValidForBid()) {
            throw new CustomException(AuctionErrorCode.INVALID_PURCHASE_METHOD);
        }
        
        if (activationMethod == null) {
            throw new CustomException(AuctionErrorCode.INVALID_ACTIVATION_METHOD);
        }
        
        if (!activationMethod.isValidForBid()) {
            throw new CustomException(AuctionErrorCode.INVALID_ACTIVATION_METHOD);
        }
        
        // 번호이동이나 기기변경 시 기존 통신사 정보 필요
        if (purchaseMethod.requiresCurrentCarrier() && currentCarrier == null) {
            throw new CustomException(AuctionErrorCode.MISSING_CURRENT_CARRIER);
        }
    }
} 