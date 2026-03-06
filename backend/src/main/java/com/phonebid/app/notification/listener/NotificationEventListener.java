package com.phonebid.app.notification.listener;

import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.event.*;
import com.phonebid.app.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

/**
 * 알림 이벤트 리스너
 * 도메인 이벤트를 수신하여 알림 생성 및 발송 처리
 * 
 * @TransactionalEventListener를 사용하여 트랜잭션 커밋 후에만 알림 발송
 * @Async를 사용하여 비동기 처리 (별도 쓰레드 풀 사용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * 이벤트에 포함된 사용자에게 알림 전송 (단일 사용자)
     * 
     * @param event 알림 이벤트
     * @param channels 알림 채널 목록
     */
    private void sendNotificationToEventUser(NotificationEvent event, List<NotificationChannel> channels) {
        if (event.getUserId() == null) {
            return;
        }
        
        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user != null) {
            notificationService.createAndSendNotification(
                    user, event.getNotificationType(), channels, event.getReferenceId());
        }
    }

    /**
     * 사용자 ID로 알림 전송
     * 
     * @param userId 사용자 ID
     * @param event 알림 이벤트
     * @param channels 알림 채널 목록
     */
    private void sendNotificationToUser(UUID userId, NotificationEvent event, List<NotificationChannel> channels) {
        if (userId == null) {
            return;
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            notificationService.createAndSendNotification(
                    user, event.getNotificationType(), channels, event.getReferenceId());
        }
    }

    /**
     * 여러 사용자에게 알림 전송
     * 
     * @param users 사용자 목록
     * @param event 알림 이벤트
     * @param channels 알림 채널 목록
     */
    private void sendNotificationToUsers(List<User> users, NotificationEvent event, List<NotificationChannel> channels) {
        for (User user : users) {
            notificationService.createAndSendNotification(
                    user, event.getNotificationType(), channels, event.getReferenceId());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuoteCreated(QuoteCreatedEvent event) {
        log.debug("견적 등록 이벤트 처리: quoteId={}", event.getReferenceId());
        
        // 판매자에게 알림 발송
        List<User> sellers = userRepository.findByRole(Role.SELLER);
        sendNotificationToUsers(sellers, event, List.of(NotificationChannel.SSE));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuoteExpiringSoon(QuoteExpiringSoonEvent event) {
        log.debug("견적 마감 임박 이벤트 처리: quoteId={}", event.getReferenceId());
        sendNotificationToEventUser(event, List.of(NotificationChannel.SSE));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidArrived(BidArrivedEvent event) {
        log.debug("입찰 도착 이벤트 처리: bidId={}", event.getReferenceId());
        sendNotificationToEventUser(event, List.of(NotificationChannel.SSE));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidSelected(BidSelectedEvent event) {
        log.debug("입찰 선택 이벤트 처리: bidId={}", event.getReferenceId());
        sendNotificationToEventUser(event, List.of(NotificationChannel.SSE, NotificationChannel.KAKAO));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLowestPriceUpdated(LowestPriceUpdatedEvent event) {
        log.debug("최저가 갱신 이벤트 처리: bidId={}", event.getReferenceId());
        
        // 구매자와 판매자 모두에게 알림 발송
        List<NotificationChannel> channels = List.of(NotificationChannel.SSE, NotificationChannel.KAKAO);
        sendNotificationToUser(event.getUserId(), event, channels);
        sendNotificationToUser(event.getSellerUserId(), event, channels);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContractSigned(ContractSignedEvent event) {
        log.debug("계약 체결 이벤트 처리: contractId={}", event.getReferenceId());
        
        // 구매자와 판매자 모두에게 알림 발송
        List<NotificationChannel> channels = List.of(NotificationChannel.SSE, NotificationChannel.KAKAO);
        sendNotificationToUser(event.getUserId(), event, channels);
        sendNotificationToUser(event.getSellerUserId(), event, channels);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.debug("결제 완료 이벤트 처리: paymentId={}", event.getReferenceId());
        
        // 구매자와 판매자 모두에게 알림 발송
        List<NotificationChannel> channels = List.of(NotificationChannel.SSE, NotificationChannel.KAKAO);
        sendNotificationToUser(event.getUserId(), event, channels);
        sendNotificationToUser(event.getSellerUserId(), event, channels);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        log.debug("배송 상태 변경 이벤트 처리: deliveryId={}, newStatus={}", 
                  event.getReferenceId(), event.getNewStatus());
        
        // READY 상태는 내부 상태로 알림 발송하지 않음
        if (!event.shouldNotify()) {
            log.debug("배송 준비 중 상태는 알림 발송하지 않음: deliveryId={}", event.getReferenceId());
            return;
        }
        
        sendNotificationToEventUser(event, List.of(NotificationChannel.SSE, NotificationChannel.KAKAO));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageReceived(ChatMessageReceivedEvent event) {
        log.debug("채팅 메시지 수신 이벤트 처리: chatMessageId={}", event.getReferenceId());
        sendNotificationToEventUser(event, List.of(NotificationChannel.SSE));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSellerApprovalRequested(SellerApprovalRequestedEvent event) {
        log.debug("판매자 승인 요청 이벤트 처리: sellerId={}", event.getReferenceId());
        
        // 관리자에게 알림 발송
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        sendNotificationToUsers(admins, event, List.of(NotificationChannel.SSE));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportReceived(ReportReceivedEvent event) {
        log.debug("신고 접수 이벤트 처리: reportId={}", event.getReferenceId());
        
        // 관리자에게 알림 발송
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        sendNotificationToUsers(admins, event, List.of(NotificationChannel.SSE));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStatisticsSummary(StatisticsSummaryEvent event) {
        log.debug("통계 요약 이벤트 처리");
        
        // 관리자에게 알림 발송
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        sendNotificationToUsers(admins, event, List.of(NotificationChannel.SSE));
    }
}

