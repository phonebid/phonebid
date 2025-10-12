package com.phonebid.app.trade.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgPaymentRequest {
    @NotBlank
    private String merchantUid;     // 주문 고유 ID
    @NotNull
    @Min(1)
    private Integer amount;         // 결제 금액
    @NotBlank
    private String productName;     // 상품명
    @NotBlank
    private String buyerName;       // 구매자 이름
    @Email
    private String buyerEmail;      // 구매자 이메일
    @NotBlank
    private String buyerPhone;      // 구매자 전화번호
    private String returnUrl;       // 결제 완료 후 리턴 URL
    private String cancelUrl;       // 결제 취소 시 리턴 URL
}
