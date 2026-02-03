package com.phonebid.app.notification.service;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationType;

import java.util.List;
import java.util.UUID;

/**
 * 알림 그룹화 서비스 (구조만 구현)
 * 단시간 내 동일 타입 알림 발생 시 요약 메시지 형태로 그룹화
 * 
 * TODO: 그룹화 로직 구현
 * - 5분 이내 동일 타입 알림을 하나로 묶기
 * - 예: "입찰 3건이 도착했습니다" 형태로 요약
 */
public interface NotificationGroupingService {
    
    /**
     * 알림 그룹화 처리
     * 
     * @param userId 사용자 ID
     * @param notifications 그룹화할 알림 목록
     * @return 그룹화된 알림 목록
     */
    List<Notification> groupNotifications(UUID userId, List<Notification> notifications);

    /**
     * 특정 타입의 알림이 그룹화 대상인지 확인
     * 
     * @param type 알림 타입
     * @return 그룹화 가능 여부
     */
    boolean isGroupable(NotificationType type);
}

