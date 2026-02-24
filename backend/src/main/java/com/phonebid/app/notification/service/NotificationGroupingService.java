package com.phonebid.app.notification.service;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.dto.response.NotificationDisplayItem;

import java.util.List;
import java.util.UUID;

/**
 * 알림 그룹화 서비스
 * 단시간(5분) 내 동일 타입·동일 채널 알림 발생 시 요약 메시지 형태로 그룹화한다.
 * 예: "입찰 3건이 도착했습니다"
 */
public interface NotificationGroupingService {

    /**
     * 알림 그룹화 처리
     * type + channel 기준으로, 5분 시간 윈도우 내 연속 알림을 하나로 묶는다.
     *
     * @param userId        사용자 ID
     * @param notifications 그룹화할 알림 목록 (createdAt DESC 정렬 가정)
     * @return 그룹화된 표시용 알림 목록 (대표 알림 + 요약 메시지)
     */
    List<NotificationDisplayItem> groupNotifications(UUID userId, List<Notification> notifications);

    /**
     * 특정 타입의 알림이 그룹화 대상인지 확인
     *
     * @param type 알림 타입
     * @return 그룹화 가능 여부
     */
    boolean isGroupable(NotificationType type);
}
