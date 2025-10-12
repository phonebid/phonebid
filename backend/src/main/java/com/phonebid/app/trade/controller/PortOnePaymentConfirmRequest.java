package com.phonebid.app.trade.controller;

import jakarta.validation.constraints.NotBlank;

public record PortOnePaymentConfirmRequest(
        @NotBlank String paymentId
) {
}

