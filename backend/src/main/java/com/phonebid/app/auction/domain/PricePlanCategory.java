package com.phonebid.app.auction.domain;

public enum PricePlanCategory {
    FIVE_G("5G"),
    LTE("LTE");

    private final String displayName;

    PricePlanCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
