package com.phonebid.app.trade.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Courier {
    CJ_LOGISTICS("CJ대한통운", "CJ"),
    HANJIN("한진택배", "HANJIN"),
    LOTTE("롯데택배", "LOTTE");

    private final String displayName;
    private final String shortName;

    public boolean isCJ() {
        return this == CJ_LOGISTICS;
    }

    public boolean isHanjin() {
        return this == HANJIN;
    }

    public boolean isLotte() {
        return this == LOTTE;
    }

    public String getTrackingUrl(String invoiceNumber) {
        return switch (this) {
            case CJ_LOGISTICS -> "https://www.cjlogistics.com/ko/tool/parcel/tracking?gnbInvcNo=" + invoiceNumber;
            case HANJIN -> "https://www.hanjin.co.kr/kor/CMS/DeliveryMgr/WaybillResult.do?mCode=MN038&schLang=KR&wblnumText2=" + invoiceNumber;
            case LOTTE -> "https://www.lotteglogis.com/home/reservation/tracking/linkView?InvNo=" + invoiceNumber;
        };
    }
} 