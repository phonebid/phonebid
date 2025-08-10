package com.phonebid.app.auction.domain;

import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricePlan {

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "plan_price")
    private Integer planPrice;

    @Builder
    public PricePlan(String planName, Integer planPrice) {
        validatePricePlan(planName, planPrice);
        
        this.planName = planName;
        this.planPrice = planPrice;
    }

    // 비즈니스 메서드
    public String getPlanSummary() {
        if (planName == null || planPrice == null) {
            return "요금제 정보 없음";
        }
        return String.format("%s (%,d원)", planName, planPrice);
    }

    public boolean isComplete() {
        return planName != null && !planName.trim().isEmpty() && 
               planPrice != null && planPrice > 0;
    }

    public boolean isEmpty() {
        return (planName == null || planName.trim().isEmpty()) && 
               (planPrice == null || planPrice == 0);
    }

    public boolean isAffordable(Integer budget) {
        if (planPrice == null || budget == null) {
            return false;
        }
        return planPrice <= budget;
    }

    public boolean isUnlimited() {
        if (planName == null) {
            return false;
        }
        String lowerName = planName.toLowerCase();
        return lowerName.contains("무제한") || lowerName.contains("unlimited");
    }

    // 검증 메서드
    private void validatePricePlan(String planName, Integer planPrice) {
        if (planName != null && planName.trim().isEmpty()) {
            throw new CustomException(AuctionErrorCode.INVALID_PRICE_PLAN_NAME);
        }
        
        if (planPrice != null && planPrice < 0) {
            throw new CustomException(AuctionErrorCode.INVALID_PRICE_PLAN_PRICE);
        }
        
        if (planName != null && planName.length() > 100) {
            throw new CustomException(AuctionErrorCode.INVALID_PRICE_PLAN_NAME);
        }
    }
} 