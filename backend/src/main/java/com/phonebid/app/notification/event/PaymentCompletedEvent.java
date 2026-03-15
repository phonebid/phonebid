package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.trade.domain.Payment;

import java.util.UUID;

/**
 * 결제 완료 이벤트
 * 구매자와 판매자 모두에게 결제 완료 알림 발송
 */
public class PaymentCompletedEvent extends NotificationEvent {
    private final Payment payment;
    private final UUID sellerUserId;

    public PaymentCompletedEvent(Object source, Payment payment) {
        super(source, payment.getContract().getQuote().getUser().getId(), 
              NotificationType.PAYMENT_COMPLETED, payment.getId());
        this.payment = payment;
        this.sellerUserId = payment.getContract().getSelectedBid().getSeller().getUser().getId();
    }

    public Payment getPayment() {
        return payment;
    }

    public UUID getSellerUserId() {
        return sellerUserId;
    }
}

