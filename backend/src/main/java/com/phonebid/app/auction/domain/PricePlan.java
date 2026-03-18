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
@Table(name = "price_plans", indexes = {
    @Index(name = "idx_price_plans_carrier_active", columnList = "carrier, is_active"),
    @Index(name = "idx_price_plans_category_active", columnList = "category, is_active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricePlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("요금제 고유 ID (UUID)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    @Comment("통신사 (SKT, KT, LGU)")
    private Carrier carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "varchar(255) default 'FIVE_G'")
    @Comment("요금제 구분 (5G, LTE)")
    private PricePlanCategory category;

    @Column(name = "plan_name", nullable = false)
    @Comment("요금제 이름")
    private String planName;

    @Column(name = "monthly_fee", nullable = false)
    @Comment("월정액 (VAT 포함, 원)")
    private Integer monthlyFee;

    @Column(name = "data_allowance_text")
    @Comment("데이터 제공량 (예: 무제한, 110GB)")
    private String dataAllowanceText;

    @Column(name = "throttle_speed_text")
    @Comment("소진 시 속도 (예: 5Mbps, 1Mbps, -)")
    private String throttleSpeedText;

    @Column(name = "voice_sms_text")
    @Comment("음성/문자 (예: 집/이동전화 무제한)")
    private String voiceSmsText;

    @Column(name = "is_active", nullable = false)
    @Comment("사용 여부 (true: 활성, false: 비활성)")
    private Boolean isActive;

    @Column(name = "display_order")
    @Comment("노출 순서 (낮을수록 상위)")
    private Integer displayOrder;

    @Builder
    private PricePlan(Carrier carrier, PricePlanCategory category, String planName, 
                      Integer monthlyFee, String dataAllowanceText, String throttleSpeedText,
                      String voiceSmsText, Boolean isActive, Integer displayOrder) {
        this.carrier = carrier;
        this.category = category;
        this.planName = planName;
        this.monthlyFee = monthlyFee;
        this.dataAllowanceText = dataAllowanceText;
        this.throttleSpeedText = throttleSpeedText;
        this.voiceSmsText = voiceSmsText;
        this.isActive = isActive != null ? isActive : true;
        this.displayOrder = displayOrder;
    }

    public String getPlanSummary() {
        if (planName == null || monthlyFee == null) {
            return "요금제 정보 없음";
        }
        return String.format("%s (%,d원)", planName, monthlyFee);
    }

    public boolean isComplete() {
        return planName != null && !planName.trim().isEmpty() && 
               monthlyFee != null && monthlyFee > 0;
    }

    public boolean isEmpty() {
        return (planName == null || planName.trim().isEmpty()) && 
               (monthlyFee == null || monthlyFee == 0);
    }

    public boolean isAffordable(Integer budget) {
        if (budget == null || monthlyFee == null) {
            return false;
        }
        return budget >= monthlyFee;
    }

    public boolean isUnlimitedData() {
        if (dataAllowanceText == null) {
            return false;
        }
        return dataAllowanceText.contains("무제한");
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Deprecated
    public Integer getPlanPrice() {
        return this.monthlyFee;
    }
} 