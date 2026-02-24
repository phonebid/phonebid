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

        List<Notification> sorted = notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();

        Map<GroupKey, List<Notification>> byTypeAndChannel = new LinkedHashMap<>();
        for (Notification n : sorted) {
            GroupKey key = new GroupKey(n.getType(), n.getChannel());
            byTypeAndChannel.computeIfAbsent(key, k -> new ArrayList<>()).add(n);
        }

        List<NotificationDisplayItem> result = new ArrayList<>();
        for (Map.Entry<GroupKey, List<Notification>> entry : byTypeAndChannel.entrySet()) {
            List<Notification> group = entry.getValue();
            NotificationType type = entry.getKey().type;

            if (isGroupable(type)) {
                result.addAll(applyTimeWindowGrouping(group));
            } else {
                result.addAll(group.stream().map(NotificationDisplayItem::from).toList());
            }
        }

        result.sort(Comparator.comparing(NotificationDisplayItem::createdAt).reversed());
        log.debug("알림 그룹화 완료: userId={}, 원본={}, 결과={}", userId, notifications.size(), result.size());
        return result;
    }

    private List<NotificationDisplayItem> applyTimeWindowGrouping(List<Notification> group) {
        List<NotificationDisplayItem> result = new ArrayList<>();
        int i = 0;
        while (i < group.size()) {
            Notification pivot = group.get(i);
            LocalDateTime windowStart = pivot.getCreatedAt().minusMinutes(GROUP_WINDOW_MINUTES);
            int j = i;
            while (j < group.size() && !group.get(j).getCreatedAt().isBefore(windowStart)) {
                j++;
            }
            int count = j - i;
            if (count > 1) {
                String format = pivot.getType().getGroupedMessageFormat(count);
                String message = format != null ? String.format(format, count) : pivot.getMessage();
                result.add(NotificationDisplayItem.fromGrouped(pivot, count, message));
            } else {
                result.add(NotificationDisplayItem.from(pivot));
            }
            i = j;
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
