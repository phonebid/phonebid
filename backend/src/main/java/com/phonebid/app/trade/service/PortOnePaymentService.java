package com.phonebid.app.trade.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.phonebid.app.common.config.PortOneV2Properties;
import com.phonebid.app.trade.dto.request.PgPaymentRequest;
import com.phonebid.app.trade.dto.response.PortOnePaymentInitResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortOnePaymentService {

    private final PortOneV2Properties portOneV2Properties;

    public PortOnePaymentInitResponse preparePayment(PgPaymentRequest request) {
        String paymentId = generatePaymentId();

        return PortOnePaymentInitResponse.builder()
                .storeId(portOneV2Properties.getStoreId())
                .channelKey(portOneV2Properties.getChannelKey())
                .paymentId(paymentId)
                .orderName(request.getProductName())
                .amount(request.getAmount())
                .buyerName(request.getBuyerName())
                .buyerEmail(request.getBuyerEmail())
                .buyerPhone(request.getBuyerPhone())
                .redirectUrl(request.getReturnUrl())
                .cancelUrl(request.getCancelUrl())
                .build();
    }

    private String generatePaymentId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "pay-" + uuid;
    }
}

