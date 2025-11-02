package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class QuoteResponseDto {

    private UUID id;
    private PhoneModel phoneModel;
    private PhoneOption storage;
    private Carrier carrier;
    private PhoneOption color;
    private QuoteStatus status;
    private LocalDateTime expiredAt;
    private PurchaseMethod purchaseMethod;
    private Carrier currentCarrier;
    private ActivationMethod activationMethod;
    private LocalDateTime createdAt;

    public static QuoteResponseDto from(Quote quote) {
        return QuoteResponseDto.builder()
                .id(quote.getId())
                .phoneModel(quote.getPhoneModel())
                .storage(quote.getStorage())
                .carrier(quote.getCarrier())
                .color(quote.getColor())
                .status(quote.getStatus())
                .expiredAt(quote.getExpiredAt())
                .purchaseMethod(quote.getPurchaseMethod())
                .currentCarrier(quote.getCurrentCarrier())
                .activationMethod(quote.getActivationMethod())
                .createdAt(quote.getCreatedAt())
                .build();
    }
}
