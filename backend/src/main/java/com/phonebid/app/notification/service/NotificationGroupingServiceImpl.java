package com.phonebid.app.notification.service;

import com.phonebid.app.common.Constants;
import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.dto.response.NotificationDisplayItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 알림 그룹화 서비스 구현
 * type + channel 기준으로 5분 시간 윈도우 내 연속 알림을 요약 메시지로 묶는다.
 */
@Slf4j
@Service
public class NotificationGroupingServiceImpl implements NotificationGroupingService {

    private static final int GROUP_WINDOW_MINUTES = Constants.NotificationGroup.TIME_WINDOW_MINUTES;

    @Override
    public List<NotificationDisplayItem> groupNotifications(UUID userId, List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        // 1. 최신순 정렬 (createdAt 내림차순)
        List<Notification> sorted = notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();

        // 2. type + channel 기준으로 분류 (예: BID_ARRIVED+SSE, QUOTE_CREATED+SSE)
        Map<GroupKey, List<Notification>> byTypeAndChannel = new LinkedHashMap<>();
        for (Notification n : sorted) {
            GroupKey key = new GroupKey(n.getType(), n.getChannel());
            byTypeAndChannel.computeIfAbsent(key, k -> new ArrayList<>()).add(n);
        }

        // 3. 각 그룹별로 시간 윈도우 그룹화 적용
        List<NotificationDisplayItem> result = new ArrayList<>();
        for (Map.Entry<GroupKey, List<Notification>> entry : byTypeAndChannel.entrySet()) {
            List<Notification> group = entry.getValue();
            NotificationType type = entry.getKey().type;

            if (isGroupable(type)) {
                // 그룹화 가능 타입: 5분 윈도우로 묶음
                result.addAll(applyTimeWindowGrouping(group));
            } else {
                // 그룹화 불가 타입: 개별 알림으로 유지
                result.addAll(group.stream().map(NotificationDisplayItem::from).toList());
            }
        }

        // 4. 최종 결과를 다시 최신순 정렬
        result.sort(Comparator.comparing(NotificationDisplayItem::createdAt).reversed());
        log.debug("알림 그룹화 완료: userId={}, 원본={}, 결과={}", userId, notifications.size(), result.size());
        return result;
    }

    /**
     * 시간 윈도우 기반 그룹화 (5분 이내 연속 알림을 하나로 묶음)
     * 
     * 예시: 10:00, 10:02, 10:03, 10:10 알림이 있으면
     *      → [10:00~10:03] 3건 그룹 + [10:10] 단일 알림
     */
    private List<NotificationDisplayItem> applyTimeWindowGrouping(List<Notification> group) {
        List<NotificationDisplayItem> result = new ArrayList<>();
        int i = 0;
        while (i < group.size()) {
            // 현재 알림을 pivot으로 설정
            Notification pivot = group.get(i);
            LocalDateTime windowStart = pivot.getCreatedAt().minusMinutes(GROUP_WINDOW_MINUTES);
            
            // pivot 기준 5분 이내 알림들을 모두 찾기
            int j = i;
            while (j < group.size() && !group.get(j).getCreatedAt().isBefore(windowStart)) {
                j++;
            }
            int count = j - i;
            
            // 2개 이상이면 그룹화, 1개면 단일 알림
            if (count > 1) {
                String format = pivot.getType().getGroupedMessageFormat(count);
                String message = format != null ? String.format(format, count) : pivot.getMessage();
                result.add(NotificationDisplayItem.fromGrouped(pivot, count, message));
            } else {
                result.add(NotificationDisplayItem.from(pivot));
            }
            i = j;  // 다음 미처리 알림으로 이동
        }
        return result;
    }

    @Override
    public boolean isGroupable(NotificationType type) {
        return type == NotificationType.BID_ARRIVED
                || type == NotificationType.QUOTE_CREATED
                || type == NotificationType.LOWEST_PRICE_UPDATED
                || type == NotificationType.CHAT_MESSAGE_RECEIVED;
    }

    private record GroupKey(NotificationType type, NotificationChannel channel) {
    }
}
