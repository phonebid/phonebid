package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.phone.dto.response.PhoneModelResponseDto;
import com.phonebid.app.phone.dto.response.PhoneOptionResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class QuoteResponseDto {

    private UUID id;
    private PhoneModelResponseDto phoneModel;
    private PhoneOptionResponseDto storage; // nullable
    private Carrier carrier;
    private PhoneOptionResponseDto color; // nullable
    private QuoteStatus status;
    private LocalDateTime expiredAt;
    private PurchaseMethod purchaseMethod;
    private Carrier currentCarrier;
    private ActivationMethod activationMethod;
    private LocalDateTime createdAt;

    public static QuoteResponseDto from(Quote quote) {
        return QuoteResponseDto.builder()
                .id(quote.getId())
                .phoneModel(PhoneModelResponseDto.fromWithoutOptions(quote.getPhoneModel()))
                .storage(quote.getStorage() != null ? PhoneOptionResponseDto.from(quote.getStorage()) : null)
                .carrier(quote.getCarrier())
                .color(quote.getColor() != null ? PhoneOptionResponseDto.from(quote.getColor()) : null)
                .status(quote.getStatus())
                .expiredAt(quote.getExpiredAt())
                .purchaseMethod(quote.getPurchaseMethod())
                .currentCarrier(quote.getCurrentCarrier())
                .activationMethod(quote.getActivationMethod())
                .createdAt(quote.getCreatedAt())
                .build();
    }
}
