package com.phonebid.app.auction.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class BidUpdateRequestDto {

    @Min(value = 0, message = "입찰가는 0 이상이어야 합니다.")
    private Integer price;

    @Min(value = 1, message = "배송 예상일은 1일 이상이어야 합니다.")
    private Integer deliveryDays;

    private Integer additionalSubsidy;

    @Min(value = 0, message = "할부원금은 0 이상이어야 합니다.")
    private Integer installmentPrincipal;

    private Integer contractMonths;

    private String pricePlanName;

    @Min(value = 0, message = "요금제 가격은 0 이상이어야 합니다.")
    private Integer pricePlanPrice;

    @Valid
    private List<AdditionalServiceRequestDto> additionalServices;

    @Builder
    public BidUpdateRequestDto(Integer price, Integer deliveryDays, Integer additionalSubsidy,
                                Integer installmentPrincipal, Integer contractMonths,
                                String pricePlanName, Integer pricePlanPrice,
                                List<AdditionalServiceRequestDto> additionalServices) {
        this.price = price;
        this.deliveryDays = deliveryDays;
        this.additionalSubsidy = additionalSubsidy;
        this.installmentPrincipal = installmentPrincipal;
        this.contractMonths = contractMonths;
        this.pricePlanName = pricePlanName;
        this.pricePlanPrice = pricePlanPrice;
        this.additionalServices = additionalServices != null ? additionalServices : new ArrayList<>();
    }
}

