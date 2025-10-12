package com.phonebid.app.trade.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortOnePaymentStatusResponse {

    private final String paymentId;
    private final String status;
    private final Long amount;
    private final String currency;
}

