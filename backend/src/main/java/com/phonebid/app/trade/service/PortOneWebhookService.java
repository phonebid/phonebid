package com.phonebid.app.trade.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.phonebid.app.common.config.PortOneV2Properties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortOneWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PortOneWebhookService.class);

    private final PortOneV2Properties portOneV2Properties;
    private final PortOnePaymentService portOnePaymentService;

    public void handleWebhook(String payload, String webhookId, String webhookTimestamp, String webhookSignature) {
        if (!verifySignature(payload, webhookId, webhookTimestamp, webhookSignature)) {
            throw new IllegalArgumentException("포트원 웹훅 서명 검증 실패");
        }

        log.info("포트원 웹훅 수신: payload={}", payload);
    }

    private boolean verifySignature(String payload, String webhookId, String webhookTimestamp, String webhookSignature) {
        try {
            String dataToSign = webhookId + webhookTimestamp + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(portOneV2Properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(rawHmac);
            return expectedSignature.equals(webhookSignature);
        } catch (Exception e) {
            log.error("웹훅 서명 검증 중 오류", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

