package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PricePlan;
import com.phonebid.app.auction.domain.PricePlanCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PricePlanResponseDto {

    private UUID id;
    private Carrier carrier;
    private PricePlanCategory category;
    private String planName;
    private Integer monthlyFee;
    private String dataAllowanceText;
    private String throttleSpeedText;
    private String voiceSmsText;
    private Boolean isActive;
    private Integer displayOrder;

    public static PricePlanResponseDto from(PricePlan pricePlan) {
        return PricePlanResponseDto.builder()
                .id(pricePlan.getId())
                .carrier(pricePlan.getCarrier())
                .category(pricePlan.getCategory())
                .planName(pricePlan.getPlanName())
                .monthlyFee(pricePlan.getMonthlyFee())
                .dataAllowanceText(pricePlan.getDataAllowanceText())
                .throttleSpeedText(pricePlan.getThrottleSpeedText())
                .voiceSmsText(pricePlan.getVoiceSmsText())
                .isActive(pricePlan.getIsActive())
                .displayOrder(pricePlan.getDisplayOrder())
                .build();
    }
}
