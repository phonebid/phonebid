package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.trade.domain.Delivery;
import com.phonebid.app.trade.domain.DeliveryStatus;

/**
 * 배송 상태 변경 이벤트
 * 구매자에게 배송 상태 변경 알림 발송
 */
public class DeliveryStatusChangedEvent extends NotificationEvent {
    private final Delivery delivery;
    private final DeliveryStatus previousStatus;
    private final DeliveryStatus newStatus;

    public DeliveryStatusChangedEvent(Object source, Delivery delivery, DeliveryStatus previousStatus, DeliveryStatus newStatus) {
        super(source, delivery.getContract().getQuote().getUser().getId(), 
              newStatus == DeliveryStatus.DELIVERED ? NotificationType.DELIVERY_COMPLETED : NotificationType.DELIVERY_STARTED, 
              delivery.getId());
        this.delivery = delivery;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public DeliveryStatus getPreviousStatus() {
        return previousStatus;
    }

    public DeliveryStatus getNewStatus() {
        return newStatus;
    }
}

