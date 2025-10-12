package com.phonebid.app.trade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.trade.service.PortOneWebhookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments/portone/webhook")
@RequiredArgsConstructor
public class PortOneWebhookController {

    private final PortOneWebhookService portOneWebhookService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature) {

        portOneWebhookService.handleWebhook(payload, webhookId, webhookTimestamp, webhookSignature);
        ApiResponse<Void> body = ApiResponse.success(
                HttpStatus.OK,
                "PortOne 웹훅 처리 완료",
                null
        );
        return ResponseEntity.ok(body);
    }
}

