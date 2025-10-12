package com.phonebid.app.trade.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgRefundResponse {
    private boolean success;        // 성공 여부
    private Integer refundAmount;   // 환불 금액
    private String message;        // 응답 메시지
}
