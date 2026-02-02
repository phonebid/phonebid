package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;

/**
 * 통계 요약 이벤트
 * 관리자에게 일일 통계 요약 알림 발송
 */
public class StatisticsSummaryEvent extends NotificationEvent {
    private final String summaryData;

    public StatisticsSummaryEvent(Object source, String summaryData) {
        super(source, null, NotificationType.STATISTICS_SUMMARY, null);
        this.summaryData = summaryData;
    }

    public String getSummaryData() {
        return summaryData;
    }
}

