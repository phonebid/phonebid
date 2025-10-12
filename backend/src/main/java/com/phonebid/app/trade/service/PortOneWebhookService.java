package com.phonebid.app.trade.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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

    public void handleWebhook(String payload, String webhookId, String webhookTimestamp, String webhookSignature) {
        if (!verifySignature(payload, webhookId, webhookTimestamp, webhookSignature)) {
            throw new IllegalArgumentException("포트원 웹훅 서명 검증 실패");
        }
    }

    private boolean verifySignature(String payload, String webhookId, String webhookTimestamp, String webhookSignature) {
        try {
            String dataToSign = webhookId + webhookTimestamp + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(portOneV2Properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            byte[] receivedSignature = hexToBytes(webhookSignature);
            return MessageDigest.isEqual(rawHmac, receivedSignature);
        } catch (Exception e) {
            log.error("웹훅 서명 검증 중 오류", e);
            return false;
        }
    }

    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("유효하지 않은 서명 형식");
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("서명에 허용되지 않은 문자가 포함되어 있습니다.");
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }
}

