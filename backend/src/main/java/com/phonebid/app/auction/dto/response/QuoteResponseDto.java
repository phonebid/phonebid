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
    private Long bidCount;
    private Integer lowestPrice;

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
                .bidCount(null) // 기본값은 null, 필요시 서비스에서 설정
                .lowestPrice(null) // 기본값은 null, 필요시 서비스에서 설정
                .build();
    }

    public static QuoteResponseDto from(Quote quote, Long bidCount) {
        return QuoteResponseDto.builder()
                .id(quote.getId())
                .phoneModel(PhoneModelResponseDto.fromWithoutOptions(quote.getPhoneModel()))
                .storage(PhoneOptionResponseDto.from(quote.getStorage()))
                .carrier(quote.getCarrier())
                .color(PhoneOptionResponseDto.from(quote.getColor()))
                .status(quote.getStatus())
                .expiredAt(quote.getExpiredAt())
                .purchaseMethod(quote.getPurchaseMethod())
                .currentCarrier(quote.getCurrentCarrier())
                .activationMethod(quote.getActivationMethod())
                .createdAt(quote.getCreatedAt())
                .bidCount(bidCount)
                .lowestPrice(null)
                .build();
    }

    public static QuoteResponseDto from(Quote quote, Long bidCount, Integer lowestPrice) {
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
                .bidCount(bidCount)
                .lowestPrice(lowestPrice)
                .build();
    }
}
