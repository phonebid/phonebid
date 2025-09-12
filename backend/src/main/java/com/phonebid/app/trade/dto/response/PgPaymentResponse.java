package com.phonebid.app.trade.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgPaymentResponse {
    private boolean success;        // 성공 여부
    private String pgTid;          // PG 거래 ID
    private String paymentUrl;     // 결제 페이지 URL
    private Integer amount;        // 결제 금액
    private String status;         // 결제 상태
    private String message;        // 응답 메시지
}
