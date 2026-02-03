package com.phonebid.app.notification.service;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 알림 그룹화 서비스 구현 (구조만)
 * 실제 그룹화 로직은 추후 구현 예정
 */
@Slf4j
@Service
public class NotificationGroupingServiceImpl implements NotificationGroupingService {

    @Override
    public List<Notification> groupNotifications(UUID userId, List<Notification> notifications) {
        // TODO: 그룹화 로직 구현
        // 현재는 그룹화 없이 그대로 반환
        log.debug("알림 그룹화 처리 (구조만): userId={}, count={}", userId, notifications.size());
        return notifications;
    }

    @Override
    public boolean isGroupable(NotificationType type) {
        // TODO: 그룹화 가능한 타입 정의
        // 현재는 모든 타입이 그룹화 불가능으로 처리
        return false;
    }
}

