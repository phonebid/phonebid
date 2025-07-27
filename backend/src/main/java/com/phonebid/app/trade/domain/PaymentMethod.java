package com.phonebid.app.trade.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드", "신용/체크카드 결제"),
    BANK("무통장입금", "계좌이체 결제"),
    MOBILE("휴대폰", "통신사 결제");

    private final String displayName;
    private final String description;

    public boolean isCard() {
        return this == CARD;
    }

    public boolean isBank() {
        return this == BANK;
    }

    public boolean isMobile() {
        return this == MOBILE;
    }

    public boolean isInstantPayment() {
        return this == CARD || this == MOBILE;
    }

    public boolean requiresManualConfirmation() {
        return this == BANK;
    }
}