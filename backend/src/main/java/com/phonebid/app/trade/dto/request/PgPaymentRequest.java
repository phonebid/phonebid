package com.phonebid.app.trade.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgPaymentRequest {
    private String merchantUid;     // 주문 고유 ID
    private Integer amount;         // 결제 금액
    private String productName;     // 상품명
    private String buyerName;       // 구매자 이름
    private String buyerEmail;      // 구매자 이메일
    private String buyerPhone;      // 구매자 전화번호
    private String returnUrl;       // 결제 완료 후 리턴 URL
    private String cancelUrl;       // 결제 취소 시 리턴 URL
}
