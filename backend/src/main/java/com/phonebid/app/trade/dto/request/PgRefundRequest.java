package com.phonebid.app.trade.dto.request;

import lombok.Builder;
import lombok.Getter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Builder
public class PgRefundRequest {
    @NotBlank
    private String pgTid;          // 원본 거래 ID
    @NotNull
    @Min(1)
    private Integer refundAmount;   // 환불 금액
    @NotBlank
    private String reason;         // 환불 사유
}
