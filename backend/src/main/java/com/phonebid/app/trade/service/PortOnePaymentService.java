package com.phonebid.app.trade.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.phonebid.app.common.config.PortOneV2Properties;
import com.phonebid.app.trade.dto.request.PgPaymentRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.trade.dto.response.PortOnePaymentInitResponse;
import com.phonebid.app.trade.dto.response.PortOnePaymentStatusResponse;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortOnePaymentService {

    private final PortOneV2Properties portOneV2Properties;
    private final PortOneClient portOneClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public PortOnePaymentStatusResponse verifyPayment(String paymentId) {
        String responseBody = portOneClient.getPayment(paymentId).blockOptional()
                .orElseThrow(() -> new IllegalStateException("PortOne 결제 조회 실패"));

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode paymentNode = root.path("payment");

            String status = paymentNode.path("status").asText();
            long amount = paymentNode.path("amount").path("total").asLong();
            String currency = paymentNode.path("currency").path("value").asText();

            return PortOnePaymentStatusResponse.builder()
                    .paymentId(paymentId)
                    .status(status)
                    .amount(amount)
                    .currency(currency)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("PortOne 응답 파싱 실패", e);
        }
    }
}

