package com.phonebid.app.auction.dto.request;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteCreateRequestDto {

    @NotNull
    private UUID phoneModelId;

    @NotNull
    private UUID storageOptionId;

    @NotNull
    private UUID colorOptionId;

    @NotNull
    private Carrier carrier;

    @NotNull
    private PurchaseMethod purchaseMethod;

    private ActivationMethod activationMethod;

    private Carrier currentCarrier;


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

