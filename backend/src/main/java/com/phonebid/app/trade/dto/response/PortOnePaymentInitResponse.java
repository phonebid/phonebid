package com.phonebid.app.trade.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortOnePaymentInitResponse {

    private final String storeId;
    private final String channelKey;
    private final String paymentId;
    private final String orderName;
    private final Integer amount;
    private final String buyerName;
    private final String buyerEmail;
    private final String buyerPhone;
    private final String redirectUrl;
    private final String cancelUrl;
}

