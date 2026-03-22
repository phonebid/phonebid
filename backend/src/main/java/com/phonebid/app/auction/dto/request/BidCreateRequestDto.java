package com.phonebid.app.auction.dto.request;

import com.phonebid.app.auction.domain.*;
import com.phonebid.app.member.domain.Seller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class BidCreateRequestDto {

    @NotNull(message = "견적 ID는 필수입니다.")
    private UUID quoteId;

    @NotNull(message = "입찰가는 필수입니다.")
    @Min(value = 0, message = "입찰가는 0 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "배송 예상일은 필수입니다.")
    @Min(value = 1, message = "배송 예상일은 1일 이상이어야 합니다.")
    private Integer deliveryDays;

    @NotNull(message = "구매방법은 필수입니다.")
    private PurchaseMethod purchaseMethod;

    @NotNull(message = "통신사는 필수입니다.")
    private Carrier carrier;

    private Carrier currentCarrier;

    @NotNull(message = "개통방법은 필수입니다.")
    private ActivationMethod activationMethod;

    private Integer additionalSubsidy;

    @NotNull(message = "할부원금은 필수입니다.")
    @Min(value = 0, message = "할부원금은 0 이상이어야 합니다.")
    private Integer installmentPrincipal;

    private Integer contractMonths;

    @NotNull(message = "요금제 ID는 필수입니다.")
    private UUID pricePlanId;

    @Valid
    private List<AdditionalServiceRequestDto> additionalServices = new ArrayList<>();

    @Builder
    public BidCreateRequestDto(UUID quoteId, Integer price, Integer deliveryDays,
                                PurchaseMethod purchaseMethod, Carrier carrier, Carrier currentCarrier,
                                ActivationMethod activationMethod, Integer additionalSubsidy,
                                Integer installmentPrincipal, Integer contractMonths,
                                UUID pricePlanId,
                                List<AdditionalServiceRequestDto> additionalServices) {
        this.quoteId = quoteId;
        this.price = price;
        this.deliveryDays = deliveryDays;
        this.purchaseMethod = purchaseMethod;
        this.carrier = carrier;
        this.currentCarrier = currentCarrier;
        this.activationMethod = activationMethod;
        this.additionalSubsidy = additionalSubsidy;
        this.installmentPrincipal = installmentPrincipal;
        this.contractMonths = contractMonths;
        this.pricePlanId = pricePlanId;
        this.additionalServices = additionalServices != null ? additionalServices : new ArrayList<>();
    }

    public Bid toBidEntity(Quote quote, Seller seller, PricePlan pricePlan, Double ratingSnapshot) {
        return Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(price)
                .deliveryDays(deliveryDays)
                .ratingSnapshot(ratingSnapshot)
                .purchaseMethod(purchaseMethod)
                .carrier(carrier)
                .currentCarrier(currentCarrier)
                .activationMethod(activationMethod)
                .additionalSubsidy(additionalSubsidy)
                .installmentPrincipal(installmentPrincipal)
                .pricePlan(pricePlan)
                .contractMonths(contractMonths != null ? contractMonths : 24)
                .build();
    }
}

