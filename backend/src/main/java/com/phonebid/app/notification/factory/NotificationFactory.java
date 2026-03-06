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
     * 
     * @param user 수신자 (null 불가)
     * @param type 알림 타입 (null 불가)
     * @param channel 알림 채널 (null 불가)
     * @param referenceId 참조 ID (null 불가)
     * @return 생성된 알림
     */
    public Notification createNotification(User user, NotificationType type, 
                                          NotificationChannel channel, UUID referenceId) {
        // Fail-fast validation
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        if (referenceId == null) {
            throw new IllegalArgumentException("referenceId must not be null");
        }
        
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
     * 
     * @param user 수신자 (null 불가)
     * @param type 알림 타입 (null 불가)
     * @param channel 알림 채널 (null 불가)
     * @param title 제목 (null 또는 빈 문자열 불가)
     * @param message 메시지 (null 또는 빈 문자열 불가)
     * @param referenceId 참조 ID (null 불가)
     * @return 생성된 알림
     */
    public Notification createNotification(User user, NotificationType type,
                                          NotificationChannel channel, 
                                          String title, String message, UUID referenceId) {
        // Fail-fast validation
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be null or blank");
        }
        if (referenceId == null) {
            throw new IllegalArgumentException("referenceId must not be null");
        }
        
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
     * 
     * @param seller 판매자 (null 불가)
     * @param quoteId 견적 ID (null 불가)
     * @return 생성된 알림
     */
    public Notification createQuoteCreatedNotification(User seller, UUID quoteId) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }
        if (quoteId == null) {
            throw new IllegalArgumentException("quoteId must not be null");
        }
        
        String title = "새로운 견적이 등록되었습니다";
        String message = "입찰 가능한 새로운 견적이 등록되었습니다. 확인해보세요!";
        return createNotification(seller, NotificationType.QUOTE_CREATED, 
                                 NotificationChannel.SSE, title, message, quoteId);
    }

    /**
     * 입찰 도착 알림 생성
     * 
     * @param consumer 소비자 (null 불가)
     * @param bidId 입찰 ID (null 불가)
     * @param price 입찰 가격 (null 불가, 양수)
     * @return 생성된 알림
     */
    public Notification createBidArrivedNotification(User consumer, UUID bidId, Integer price) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        }
        if (bidId == null) {
            throw new IllegalArgumentException("bidId must not be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative, got: " + price);
        }
        
        String title = "새로운 입찰이 도착했습니다";
        String message = String.format("%,d원의 입찰이 도착했습니다.", price);
        return createNotification(consumer, NotificationType.BID_ARRIVED, 
                                NotificationChannel.SSE, title, message, bidId);
    }

    /**
     * 최저가 갱신 알림 생성
     * 
     * @param seller 판매자 (null 불가)
     * @param bidId 입찰 ID (null 불가)
     * @param newPrice 새로운 최저가 (null 불가, 양수)
     * @return 생성된 알림
     */
    public Notification createLowestPriceUpdatedNotification(User seller, UUID bidId, Integer newPrice) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }
        if (bidId == null) {
            throw new IllegalArgumentException("bidId must not be null");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("newPrice must not be null");
        }
        if (newPrice < 0) {
            throw new IllegalArgumentException("newPrice must not be negative, got: " + newPrice);
        }
        
        String title = "최저가가 갱신되었습니다";
        String message = String.format("더 낮은 가격(%,d원)의 입찰이 등록되었습니다.", newPrice);
        return createNotification(seller, NotificationType.LOWEST_PRICE_UPDATED, 
                                 NotificationChannel.SSE, title, message, bidId);
    }

    /**
     * 견적 마감 임박 알림 생성
     * 
     * @param consumer 소비자 (null 불가)
     * @param quoteId 견적 ID (null 불가)
     * @param hoursRemaining 남은 시간(시간 단위, 0 이상)
     * @return 생성된 알림
     */
    public Notification createQuoteExpiringSoonNotification(User consumer, UUID quoteId, long hoursRemaining) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        }
        if (quoteId == null) {
            throw new IllegalArgumentException("quoteId must not be null");
        }
        if (hoursRemaining < 0) {
            throw new IllegalArgumentException("hoursRemaining must not be negative, got: " + hoursRemaining);
        }
        
        String title = "견적이 곧 마감됩니다";
        String message = String.format("견적이 %d시간 후 마감됩니다. 서둘러 입찰을 확인해보세요!", hoursRemaining);
        return createNotification(consumer, NotificationType.QUOTE_EXPIRING_SOON, 
                                 NotificationChannel.SSE, title, message, quoteId);
    }

    /**
     * 계약 체결 알림 생성
     * 
     * @param user 사용자 (null 불가)
     * @param contractId 계약 ID (null 불가)
     * @return 생성된 알림
     */
    public Notification createContractSignedNotification(User user, UUID contractId) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (contractId == null) {
            throw new IllegalArgumentException("contractId must not be null");
        }
        
        String title = "계약이 체결되었습니다";
        String message = "전자계약서가 체결되었습니다. 결제를 진행해주세요.";
        return createNotification(user, NotificationType.CONTRACT_SIGNED, 
                                NotificationChannel.SSE, title, message, contractId);
    }

    /**
     * 결제 완료 알림 생성
     * 
     * @param user 사용자 (null 불가)
     * @param paymentId 결제 ID (null 불가)
     * @param amount 결제 금액 (null 불가, 양수)
     * @return 생성된 알림
     */
    public Notification createPaymentCompletedNotification(User user, UUID paymentId, Integer amount) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (paymentId == null) {
            throw new IllegalArgumentException("paymentId must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative, got: " + amount);
        }
        
        String title = "결제가 완료되었습니다";
        String message = String.format("%,d원 결제가 완료되었습니다.", amount);
        return createNotification(user, NotificationType.PAYMENT_COMPLETED, 
                                NotificationChannel.SSE, title, message, paymentId);
    }

    /**
     * 배송 시작 알림 생성
     * 
     * @param consumer 소비자 (null 불가)
     * @param deliveryId 배송 ID (null 불가)
     * @param courier 택배사명 (null 또는 빈 문자열 불가)
     * @return 생성된 알림
     */
    public Notification createDeliveryStartedNotification(User consumer, UUID deliveryId, String courier) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        }
        if (deliveryId == null) {
            throw new IllegalArgumentException("deliveryId must not be null");
        }
        if (courier == null || courier.isBlank()) {
            throw new IllegalArgumentException("courier must not be null or blank");
        }
        
        String title = "상품이 발송되었습니다";
        String message = String.format("%s 택배로 상품이 발송되었습니다.", courier);
        return createNotification(consumer, NotificationType.DELIVERY_STARTED, 
                                NotificationChannel.SSE, title, message, deliveryId);
    }

    /**
     * 배송 완료 알림 생성
     * 
     * @param consumer 소비자 (null 불가)
     * @param deliveryId 배송 ID (null 불가)
     * @return 생성된 알림
     */
    public Notification createDeliveryCompletedNotification(User consumer, UUID deliveryId) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        }
        if (deliveryId == null) {
            throw new IllegalArgumentException("deliveryId must not be null");
        }
        
        String title = "배송이 완료되었습니다";
        String message = "상품 배송이 완료되었습니다. 수령 확인 부탁드립니다.";
        return createNotification(consumer, NotificationType.DELIVERY_COMPLETED, 
                                NotificationChannel.SSE, title, message, deliveryId);
    }

    /**
     * 채팅 메시지 수신 알림 생성
     * 
     * @param recipient 수신자 (null 불가)
     * @param chatMessageId 채팅 메시지 ID (null 불가)
     * @return 생성된 알림
     */
    public Notification createChatMessageReceivedNotification(User recipient, UUID chatMessageId) {
        if (recipient == null) {
            throw new IllegalArgumentException("recipient must not be null");
        }
        if (chatMessageId == null) {
            throw new IllegalArgumentException("chatMessageId must not be null");
        }
        
        String title = "새로운 채팅 메시지";
        String message = "새로운 채팅 메시지가 도착했습니다.";
        return createNotification(recipient, NotificationType.CHAT_MESSAGE_RECEIVED, 
                                NotificationChannel.SSE, title, message, chatMessageId);
    }
}

