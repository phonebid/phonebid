package com.phonebid.app.notification.sender;

import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.client.AligoKakaoClient;
import com.phonebid.app.notification.config.AligoProperties;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.dto.aligo.AligoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoTalkNotificationSender 단위 테스트")
class KakaoTalkNotificationSenderTest {

    @Mock
    private AligoKakaoClient aligoClient;

    @Mock
    private AligoProperties aligoProperties;

    @InjectMocks
    private KakaoTalkNotificationSender sender;

    private User testUser;
    private AligoProperties.Template mockTemplate;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .name("테스트사용자")
                .nickname("테스터")
                .phone("010-1234-5678")
                .role(Role.CONSUMER)
                .build();

        // Mock 템플릿 설정
        mockTemplate = new AligoProperties.Template();
        mockTemplate.setBidArrived("TPL_BID_001");
        mockTemplate.setBidSelected("TPL_BID_002");
        mockTemplate.setContractSigned("TPL_CONTRACT_001");
        
        lenient().when(aligoProperties.getTemplate()).thenReturn(mockTemplate);
    }

    @Test
    @DisplayName("알림톡 발송 성공")
    void sendKakaoNotification_Success() {
        // Given
        Notification notification = Notification.builder()
                .user(testUser)
                .type(NotificationType.BID_ARRIVED)
                .channel(NotificationChannel.KAKAO)
                .title("새로운 입찰 도착")
                .message("새로운 입찰이 도착했습니다.")
                .referenceId(UUID.randomUUID())
                .build();

        AligoResponse mockResponse = new AligoResponse(1, "success", 1);

        when(aligoClient.sendKakaoNotification(any()))
                .thenReturn(Mono.just(mockResponse));

        // When
        boolean result = sender.send(notification);

        // Then
        assertTrue(result);
        verify(aligoClient, times(1)).sendKakaoNotification(any());
    }

    @Test
    @DisplayName("전화번호 없음 - 발송 스킵")
    void sendKakaoNotification_NoPhoneNumber() {
        // Given
        User userWithoutPhone = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .name("테스트사용자")
                .nickname("테스터")
                .phone(null)
                .role(Role.CONSUMER)
                .build();

        Notification notification = Notification.builder()
                .user(userWithoutPhone)
                .type(NotificationType.BID_ARRIVED)
                .channel(NotificationChannel.KAKAO)
                .title("새로운 입찰 도착")
                .message("새로운 입찰이 도착했습니다.")
                .build();

        // When
        boolean result = sender.send(notification);

        // Then
        assertFalse(result);
        verify(aligoClient, never()).sendKakaoNotification(any());
    }

    @Test
    @DisplayName("템플릿 코드 없음 - 발송 스킵")
    void sendKakaoNotification_NoTemplateCode() {
        // Given
        mockTemplate.setBidArrived(null);

        Notification notification = Notification.builder()
                .user(testUser)
                .type(NotificationType.BID_ARRIVED)
                .channel(NotificationChannel.KAKAO)
                .title("새로운 입찰 도착")
                .message("새로운 입찰이 도착했습니다.")
                .build();

        // When
        boolean result = sender.send(notification);

        // Then
        assertFalse(result);
        verify(aligoClient, never()).sendKakaoNotification(any());
    }

    @Test
    @DisplayName("알림톡 발송 실패 - API 응답 실패")
    void sendKakaoNotification_ApiFailure() {
        // Given
        Notification notification = Notification.builder()
                .user(testUser)
                .type(NotificationType.BID_ARRIVED)
                .channel(NotificationChannel.KAKAO)
                .title("새로운 입찰 도착")
                .message("새로운 입찰이 도착했습니다.")
                .build();

        AligoResponse mockResponse = new AligoResponse(-1, "failure", 0);

        when(aligoClient.sendKakaoNotification(any()))
                .thenReturn(Mono.just(mockResponse));

        // When
        boolean result = sender.send(notification);

        // Then
        assertFalse(result);
        verify(aligoClient, times(1)).sendKakaoNotification(any());
    }

    @Test
    @DisplayName("알림톡 발송 예외 발생")
    void sendKakaoNotification_Exception() {
        // Given
        Notification notification = Notification.builder()
                .user(testUser)
                .type(NotificationType.BID_ARRIVED)
                .channel(NotificationChannel.KAKAO)
                .title("새로운 입찰 도착")
                .message("새로운 입찰이 도착했습니다.")
                .build();

        when(aligoClient.sendKakaoNotification(any()))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When
        boolean result = sender.send(notification);

        // Then
        assertFalse(result);
        verify(aligoClient, times(1)).sendKakaoNotification(any());
    }

    @Test
    @DisplayName("NotificationChannel 지원 확인 - KAKAO")
    void supports_Kakao() {
        // When & Then
        assertTrue(sender.supports(NotificationChannel.KAKAO));
    }

    @Test
    @DisplayName("NotificationChannel 지원 확인 - SSE (미지원)")
    void supports_Sse() {
        // When & Then
        assertFalse(sender.supports(NotificationChannel.SSE));
    }
}
