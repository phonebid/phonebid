package com.phonebid.app.notification.factory;

import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 알림 생성 팩토리 (Factory Pattern)
 * 알림 타입별 메시지 생성 로직 캡슐화
 */
@Component
@RequiredArgsConstructor
public class NotificationFactory {

    /**
     * 기본 메시지로 알림 생성
     */
    public Notification createNotification(User user, NotificationType type, 
                                          NotificationChannel channel, UUID referenceId) {
        return Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .title(type.getDisplayName())
                .message(type.getDefaultMessage())
                .referenceId(referenceId)
                .build();
    }

    /**
     * 커스텀 메시지로 알림 생성
     */
    public Notification createNotification(User user, NotificationType type,
                                          NotificationChannel channel, 
                                          String title, String message, UUID referenceId) {
        return Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();
    }

    /**
     * 견적 등록 알림 생성
     */
    public Notification createQuoteCreatedNotification(User seller, UUID quoteId) {
        String title = "새로운 견적이 등록되었습니다";
        String message = "입찰 가능한 새로운 견적이 등록되었습니다. 확인해보세요!";
        return createNotification(seller, NotificationType.QUOTE_CREATED, 
                                 NotificationChannel.SSE, title, message, quoteId);
    }

    /**
     * 입찰 도착 알림 생성
     */
    public Notification createBidArrivedNotification(User consumer, UUID bidId, Integer price) {
        String title = "새로운 입찰이 도착했습니다";
        String message = String.format("%,d원의 입찰이 도착했습니다.", price);
        return createNotification(consumer, NotificationType.BID_ARRIVED, 
                                NotificationChannel.SSE, title, message, bidId);
    }

    /**
     * 최저가 갱신 알림 생성
     */
    public Notification createLowestPriceUpdatedNotification(User seller, UUID bidId, Integer newPrice) {
        String title = "최저가가 갱신되었습니다";
        String message = String.format("더 낮은 가격(%,d원)의 입찰이 등록되었습니다.", newPrice);
        return createNotification(seller, NotificationType.LOWEST_PRICE_UPDATED, 
                                 NotificationChannel.SSE, title, message, bidId);
    }

    /**
     * 견적 마감 임박 알림 생성
     */
    public Notification createQuoteExpiringSoonNotification(User consumer, UUID quoteId, long hoursRemaining) {
        String title = "견적이 곧 마감됩니다";
        String message = String.format("견적이 %d시간 후 마감됩니다. 서둘러 입찰을 확인해보세요!", hoursRemaining);
        return createNotification(consumer, NotificationType.QUOTE_EXPIRING_SOON, 
                                 NotificationChannel.SSE, title, message, quoteId);
    }

    /**
     * 계약 체결 알림 생성
     */
    public Notification createContractSignedNotification(User user, UUID contractId) {
        String title = "계약이 체결되었습니다";
        String message = "전자계약서가 체결되었습니다. 결제를 진행해주세요.";
        return createNotification(user, NotificationType.CONTRACT_SIGNED, 
                                NotificationChannel.SSE, title, message, contractId);
    }

    /**
     * 결제 완료 알림 생성
     */
    public Notification createPaymentCompletedNotification(User user, UUID paymentId, Integer amount) {
        String title = "결제가 완료되었습니다";
        String message = String.format("%,d원 결제가 완료되었습니다.", amount);
        return createNotification(user, NotificationType.PAYMENT_COMPLETED, 
                                NotificationChannel.SSE, title, message, paymentId);
    }

    /**
     * 배송 시작 알림 생성
     */
    public Notification createDeliveryStartedNotification(User consumer, UUID deliveryId, String courier) {
        String title = "상품이 발송되었습니다";
        String message = String.format("%s 택배로 상품이 발송되었습니다.", courier);
        return createNotification(consumer, NotificationType.DELIVERY_STARTED, 
                                NotificationChannel.SSE, title, message, deliveryId);
    }

    /**
     * 배송 완료 알림 생성
     */
    public Notification createDeliveryCompletedNotification(User consumer, UUID deliveryId) {
        String title = "배송이 완료되었습니다";
        String message = "상품 배송이 완료되었습니다. 수령 확인 부탁드립니다.";
        return createNotification(consumer, NotificationType.DELIVERY_COMPLETED, 
                                NotificationChannel.SSE, title, message, deliveryId);
    }

    /**
     * 채팅 메시지 수신 알림 생성
     */
    public Notification createChatMessageReceivedNotification(User recipient, UUID chatMessageId) {
        String title = "새로운 채팅 메시지";
        String message = "새로운 채팅 메시지가 도착했습니다.";
        return createNotification(recipient, NotificationType.CHAT_MESSAGE_RECEIVED, 
                                NotificationChannel.SSE, title, message, chatMessageId);
    }
}

