package com.phonebid.app.auction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Carrier {
    SKT("SK텔레콤", "SKT"),
    KT("KT", "KT"),
    LGU("LG유플러스", "LGU+");

    private final String displayName;
    private final String shortName;

    public boolean isSKT() {
        return this == SKT;
    }

    public boolean isKT() {
        return this == KT;
    }

    public boolean isLGU() {
        return this == LGU;
    }
}
