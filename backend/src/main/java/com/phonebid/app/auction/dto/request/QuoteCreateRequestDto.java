package com.phonebid.app.auction.dto.request;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuoteCreateRequestDto {

    @NotNull
    private final UUID phoneModelId;

    @NotNull
    private final UUID storageOptionId;

    @NotNull
    private final UUID colorOptionId;

    @NotNull
    private final Carrier carrier;

    @NotNull
    private final PurchaseMethod purchaseMethod;

    @NotNull
    private final ActivationMethod activationMethod;

    private final Carrier currentCarrier;


    public Quote toEntity(User user, PhoneModel phoneModel, PhoneOption colorOption, PhoneOption storageOption) {
        return Quote.builder()
            .user(user)
            .phoneModel(phoneModel)
            .storage(storageOption)
            .color(colorOption)
            .carrier(carrier)
            .purchaseMethod(purchaseMethod)
            .activationMethod(activationMethod)
            .currentCarrier(currentCarrier)
            .build();
    }
}

