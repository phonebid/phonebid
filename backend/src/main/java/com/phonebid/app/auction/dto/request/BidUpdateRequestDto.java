package com.phonebid.app.auction.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class BidUpdateRequestDto {

    @Min(value = 0, message = "입찰가는 0 이상이어야 합니다.")
    private Integer price;

    @Min(value = 1, message = "배송 예상일은 1일 이상이어야 합니다.")
    private Integer deliveryDays;

    @Min(value = 0, message = "추가 지원금은 0 이상이어야 합니다.")
    private Integer additionalSubsidy;

    @Min(value = 0, message = "할부원금은 0 이상이어야 합니다.")
    private Integer installmentPrincipal;

    @Min(value = 1, message = "약정개월은 1개월 이상이어야 합니다.")
    private Integer contractMonths;

    private UUID pricePlanId;

    @Valid
    private List<AdditionalServiceRequestDto> additionalServices;

    @Builder
    public BidUpdateRequestDto(Integer price, Integer deliveryDays, Integer additionalSubsidy,
                                Integer installmentPrincipal, Integer contractMonths,
                                UUID pricePlanId,
                                List<AdditionalServiceRequestDto> additionalServices) {
        this.price = price;
        this.deliveryDays = deliveryDays;
        this.additionalSubsidy = additionalSubsidy;
        this.installmentPrincipal = installmentPrincipal;
        this.contractMonths = contractMonths;
        this.pricePlanId = pricePlanId;
        this.additionalServices = additionalServices != null ? additionalServices : new ArrayList<>();
    }
}

