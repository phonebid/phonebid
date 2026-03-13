package com.phonebid.app.trade.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.phonebid.app.common.config.PortOneV2Properties;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final WebClient webClient;
    private final PortOneV2Properties portOneV2Properties;

    public Mono<String> getPayment(String paymentId) {
        return webClient
                .get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "PortOne " + portOneV2Properties.getApiSecret())
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getIdentityVerification(String identityVerificationId) {
        return webClient
                .get()
                .uri("https://api.portone.io/identity-verifications/{identityVerificationId}", identityVerificationId)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "PortOne " + portOneV2Properties.getApiSecret())
                .retrieve()
                .bodyToMono(String.class);
    }
}

