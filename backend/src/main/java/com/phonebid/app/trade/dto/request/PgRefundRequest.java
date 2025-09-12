package com.phonebid.app.trade.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgRefundRequest {
    private String pgTid;          // 원본 거래 ID
    private Integer refundAmount;   // 환불 금액
    private String reason;         // 환불 사유
}
