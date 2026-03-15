package com.phonebid.app.notification.service;

import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.dto.response.NotificationDisplayItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("알림 그룹화 서비스 테스트")
class NotificationGroupingServiceImplTest {

    private NotificationGroupingService groupingService;

    private User testUser;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        groupingService = new NotificationGroupingServiceImpl();
        
        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .build();
        
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    }

    @Test
    @DisplayName("5분 이내 동일 타입 알림 3개가 1개로 그룹화된다")
    void groupNotifications_SameType_Within5Minutes_ShouldGroup() {
        // Given: BID_ARRIVED 알림 3개 (10:00, 10:02, 10:03)
        List<Notification> notifications = List.of(
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime),
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime.plusMinutes(2)),
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime.plusMinutes(3))
        );

        // When: 그룹화 실행
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 3개가 1개로 묶임
        assertThat(result).hasSize(1);
        assertThat(result.get(0).message()).isEqualTo("입찰 3건이 도착했습니다");
        assertThat(result.get(0).type()).isEqualTo(NotificationType.BID_ARRIVED);
    }

    @Test
    @DisplayName("5분 초과 시 그룹화되지 않고 분리된다")
    void groupNotifications_ExceedsTimeWindow_ShouldNotGroup() {
        // Given: 10:00과 10:10 (5분 초과)
        List<Notification> notifications = List.of(
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime),
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime.plusMinutes(10))
        );

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 2개 그대로 유지
        assertThat(result).hasSize(2);
        assertThat(result.get(0).message()).isEqualTo("새로운 입찰이 도착했습니다"); // 원본 메시지
        assertThat(result.get(1).message()).isEqualTo("새로운 입찰이 도착했습니다");
    }

    @Test
    @DisplayName("다른 타입 알림은 그룹화되지 않는다")
    void groupNotifications_DifferentTypes_ShouldNotGroup() {
        // Given: BID_ARRIVED와 QUOTE_CREATED
        List<Notification> notifications = List.of(
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime),
                createNotification(NotificationType.QUOTE_CREATED, NotificationChannel.SSE, baseTime.plusMinutes(1))
        );

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 타입이 다르므로 2개 유지
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("다른 채널 알림은 그룹화되지 않는다")
    void groupNotifications_DifferentChannels_ShouldNotGroup() {
        // Given: 같은 타입, 다른 채널 (SSE vs EMAIL)
        List<Notification> notifications = List.of(
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime),
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.EMAIL, baseTime.plusMinutes(1))
        );

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 채널이 다르므로 2개 유지
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("그룹화 불가 타입은 개별 알림으로 유지된다")
    void groupNotifications_NonGroupableType_ShouldKeepSeparate() {
        // Given: CONTRACT_SIGNED (그룹화 불가 타입) 3개
        List<Notification> notifications = List.of(
                createNotification(NotificationType.CONTRACT_SIGNED, NotificationChannel.SSE, baseTime),
                createNotification(NotificationType.CONTRACT_SIGNED, NotificationChannel.SSE, baseTime.plusMinutes(1)),
                createNotification(NotificationType.CONTRACT_SIGNED, NotificationChannel.SSE, baseTime.plusMinutes(2))
        );

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 그룹화 불가이므로 3개 유지
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("복잡한 시나리오: 여러 그룹이 혼재된 경우")
    void groupNotifications_ComplexScenario_ShouldGroupCorrectly() {
        // Given: 
        // - BID_ARRIVED 3개 (10:00, 10:02, 10:03) → 그룹화
        // - QUOTE_CREATED 2개 (10:05, 10:06) → 그룹화
        // - CONTRACT_SIGNED 1개 (10:08) → 단독
        List<Notification> notifications = List.of(
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime),
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime.plusMinutes(2)),
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime.plusMinutes(3)),
                createNotification(NotificationType.QUOTE_CREATED, NotificationChannel.SSE, baseTime.plusMinutes(5)),
                createNotification(NotificationType.QUOTE_CREATED, NotificationChannel.SSE, baseTime.plusMinutes(6)),
                createNotification(NotificationType.CONTRACT_SIGNED, NotificationChannel.SSE, baseTime.plusMinutes(8))
        );

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 3개 그룹 (BID 그룹 1개 + QUOTE 그룹 1개 + CONTRACT 단독 1개)
        assertThat(result).hasSize(3);
        
        // BID_ARRIVED 그룹 확인
        NotificationDisplayItem bidGroup = result.stream()
                .filter(item -> item.type() == NotificationType.BID_ARRIVED)
                .findFirst()
                .orElseThrow();
        assertThat(bidGroup.message()).isEqualTo("입찰 3건이 도착했습니다");
        
        // QUOTE_CREATED 그룹 확인
        NotificationDisplayItem quoteGroup = result.stream()
                .filter(item -> item.type() == NotificationType.QUOTE_CREATED)
                .findFirst()
                .orElseThrow();
        assertThat(quoteGroup.message()).isEqualTo("견적 2건이 등록되었습니다");
    }

    @Test
    @DisplayName("빈 리스트는 빈 결과를 반환한다")
    void groupNotifications_EmptyList_ShouldReturnEmpty() {
        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 리스트는 빈 결과를 반환한다")
    void groupNotifications_NullList_ShouldReturnEmpty() {
        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단일 알림은 그룹화 없이 그대로 반환된다")
    void groupNotifications_SingleNotification_ShouldReturnAsIs() {
        // Given
        List<Notification> notifications = List.of(
                createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, baseTime)
        );

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).message()).isEqualTo("새로운 입찰이 도착했습니다");
    }

    @Test
    @DisplayName("isGroupable() - 그룹화 가능 타입 확인")
    void isGroupable_GroupableTypes_ShouldReturnTrue() {
        // When & Then
        assertThat(groupingService.isGroupable(NotificationType.BID_ARRIVED)).isTrue();
        assertThat(groupingService.isGroupable(NotificationType.QUOTE_CREATED)).isTrue();
        assertThat(groupingService.isGroupable(NotificationType.LOWEST_PRICE_UPDATED)).isTrue();
        assertThat(groupingService.isGroupable(NotificationType.CHAT_MESSAGE_RECEIVED)).isTrue();
    }

    @Test
    @DisplayName("isGroupable() - 그룹화 불가 타입 확인")
    void isGroupable_NonGroupableTypes_ShouldReturnFalse() {
        // When & Then
        assertThat(groupingService.isGroupable(NotificationType.CONTRACT_SIGNED)).isFalse();
        assertThat(groupingService.isGroupable(NotificationType.PAYMENT_COMPLETED)).isFalse();
        assertThat(groupingService.isGroupable(NotificationType.BID_SELECTED)).isFalse();
    }

    @Test
    @DisplayName("10개 알림이 5분 윈도우로 2개 그룹으로 나뉜다")
    void groupNotifications_LargeSet_ShouldGroupIntoMultiple() {
        // Given: 5개 (10:00~10:04) + 5개 (10:10~10:14)
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            notifications.add(createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, 
                    baseTime.plusMinutes(i)));
        }
        for (int i = 10; i < 15; i++) {
            notifications.add(createNotification(NotificationType.BID_ARRIVED, NotificationChannel.SSE, 
                    baseTime.plusMinutes(i)));
        }

        // When
        List<NotificationDisplayItem> result = groupingService.groupNotifications(testUser.getId(), notifications);

        // Then: 2개 그룹
        assertThat(result).hasSize(2);
        assertThat(result.get(0).message()).isEqualTo("입찰 5건이 도착했습니다");
        assertThat(result.get(1).message()).isEqualTo("입찰 5건이 도착했습니다");
    }

    // === Helper 메서드 ===

    private Notification createNotification(NotificationType type, NotificationChannel channel, LocalDateTime createdAt) {
        Notification notification = Notification.builder()
                .user(testUser)
                .type(type)
                .channel(channel)
                .title(type.getDisplayName())
                .message(type.getDefaultMessage())
                .referenceId(UUID.randomUUID())
                .build();
        
        // createdAt 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field createdAtField = notification.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(notification, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt", e);
        }
        
        return notification;
    }
}
