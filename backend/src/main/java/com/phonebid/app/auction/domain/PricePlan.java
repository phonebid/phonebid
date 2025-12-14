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
@Table(name = "price_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricePlan extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("요금제 고유 ID (UUID)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier")
    @Comment("통신사")
    private Carrier carrier;
    
    @Column(name = "plan_name")
    @Comment("요금제 이름")
    private String planName;

    @Column(name = "plan_price")
    @Comment("요금제 가격")
    private Integer planPrice;

    @Builder
    private PricePlan(Carrier carrier, String planName, Integer planPrice) {
        this.carrier = carrier;
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
        if (budget == null || planPrice == null) {
            return false;
        }
        return budget >= planPrice;
    }

    public boolean isUnlimited() {
        if (planName == null) {
            return false;
        }
        String lowerPlanName = planName.toLowerCase();
        return lowerPlanName.contains("무제한") || lowerPlanName.contains("unlimited");
    }

    // 요금제 정보 업데이트
    public void update(String planName, Integer planPrice) {
        if (planName != null) {
            this.planName = planName;
        }
        if (planPrice != null) {
            this.planPrice = planPrice;
        }
    }

} 