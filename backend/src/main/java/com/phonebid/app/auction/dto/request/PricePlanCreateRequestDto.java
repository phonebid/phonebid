package com.phonebid.app.auction.dto.request;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PricePlan;
import com.phonebid.app.auction.domain.PricePlanCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PricePlanCreateRequestDto {

    @NotNull(message = "통신사는 필수입니다.")
    private Carrier carrier;

    @NotNull(message = "요금제 구분은 필수입니다.")
    private PricePlanCategory category;

    @NotBlank(message = "요금제 이름은 필수입니다.")
    private String planName;

    @NotNull(message = "월정액은 필수입니다.")
    @Min(value = 0, message = "월정액은 0 이상이어야 합니다.")
    private Integer monthlyFee;

    private String dataAllowanceText;
    private String throttleSpeedText;
    private String voiceSmsText;
    private Integer displayOrder;

    @Builder
    public PricePlanCreateRequestDto(Carrier carrier, PricePlanCategory category, String planName,
                                      Integer monthlyFee, String dataAllowanceText, String throttleSpeedText,
                                      String voiceSmsText, Integer displayOrder) {
        this.carrier = carrier;
        this.category = category;
        this.planName = planName;
        this.monthlyFee = monthlyFee;
        this.dataAllowanceText = dataAllowanceText;
        this.throttleSpeedText = throttleSpeedText;
        this.voiceSmsText = voiceSmsText;
        this.displayOrder = displayOrder;
    }

    public PricePlan toEntity() {
        return PricePlan.builder()
                .carrier(carrier)
                .category(category)
                .planName(planName)
                .monthlyFee(monthlyFee)
                .dataAllowanceText(dataAllowanceText)
                .throttleSpeedText(throttleSpeedText)
                .voiceSmsText(voiceSmsText)
                .isActive(true)
                .displayOrder(displayOrder)
                .build();
    }
}
