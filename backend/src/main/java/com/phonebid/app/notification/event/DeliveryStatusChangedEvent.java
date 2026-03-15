package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.trade.domain.Delivery;
import com.phonebid.app.trade.domain.DeliveryStatus;

/**
 * 배송 상태 변경 이벤트
 * 구매자에게 배송 상태 변경 알림 발송
 * READY 상태는 내부 상태로 알림 발송하지 않음
 */
public class DeliveryStatusChangedEvent extends NotificationEvent {
    private final Delivery delivery;
    private final DeliveryStatus previousStatus;
    private final DeliveryStatus newStatus;
    private final boolean shouldNotify;

    public DeliveryStatusChangedEvent(Object source, Delivery delivery, DeliveryStatus previousStatus, DeliveryStatus newStatus) {
        super(source, delivery.getContract().getQuote().getUser().getId(), 
              mapDeliveryStatusToNotificationType(newStatus), 
              delivery.getId());
        this.delivery = delivery;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.shouldNotify = shouldSendNotification(newStatus);
    }

    /**
     * DeliveryStatus를 NotificationType으로 명시적 매핑
     * READY: 배송 준비 중 (알림 불필요)
     * SHIPPED: 배송 시작 (알림 발송됨)
     * DELIVERED: 배송 완료
     */
    private static NotificationType mapDeliveryStatusToNotificationType(DeliveryStatus status) {
        return switch (status) {
            case READY -> null; // 알림 불필요
            case SHIPPED -> NotificationType.DELIVERY_STARTED;
            case DELIVERED -> NotificationType.DELIVERY_COMPLETED;
        };
    }

    /**
     * 알림 발송 필요 여부 확인
     * READY 상태는 내부 처리 상태로 사용자에게 알림 불필요
     */
    private static boolean shouldSendNotification(DeliveryStatus status) {
        return status != DeliveryStatus.READY;
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

    public boolean shouldNotify() {
        return shouldNotify;
    }
}

