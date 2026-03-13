package com.phonebid.app.auction.domain;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Carrier {
    SKT("SK텔레콤", "SKT"),
    KT("KT", "KT"),
    LGU("LG유플러스", "LG"),
    SKT_ALD("SKT알뜰폰", "SKT_A"),
    KT_ALD("KT알뜰폰", "KT_A"),
    LGU_ALD("LGU+알뜰폰", "LG_A"),
    ANY("상관 없음", "ANY");

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

    public boolean isMVNO() {
        return this == SKT_ALD || this == KT_ALD || this == LGU_ALD;
    }

    public boolean isMajor() {
        return this == SKT || this == KT || this == LGU;
    }

    public static List<Carrier> getMajorCarriersExcluding(Carrier current) {
        return Arrays.stream(values())
                .filter(c -> c.isMajor() && c != current)
                .toList();
    }
}
