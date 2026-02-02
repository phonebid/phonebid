package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;

import java.util.UUID;

/**
 * 신고 접수 이벤트
 * 관리자에게 신고 접수 알림 발송
 */
public class ReportReceivedEvent extends NotificationEvent {
    private final UUID reportId;
    private final String reportType;

    public ReportReceivedEvent(Object source, UUID reportId, String reportType) {
        super(source, null, NotificationType.REPORT_RECEIVED, reportId);
        this.reportId = reportId;
        this.reportType = reportType;
    }

    public UUID getReportId() {
        return reportId;
    }

    public String getReportType() {
        return reportType;
    }
}

