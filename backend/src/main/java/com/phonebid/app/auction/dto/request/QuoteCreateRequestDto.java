package com.phonebid.app.auction.dto.request;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuoteCreateRequestDto {

    @NotBlank
    private final String model;

    @NotBlank
    private final String storage;

    @NotBlank
    private final String color;

    @NotNull
    private final Carrier carrier;

    @NotNull
    private final PurchaseMethod purchaseMethod;

    @NotNull
    private final ActivationMethod activationMethod;

    private final Carrier currentCarrier;

    @NotNull
    @jakarta.validation.constraints.Positive
    private final Integer hopePrice;
}

