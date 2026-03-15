package com.phonebid.app.notification.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 알림 수신 설정 엔티티
 * 사용자별, 알림 타입별, 채널별 수신 동의를 관리
 * 법적 요구사항(마케팅 알림 동의) 대응을 위한 설계
 */
@Entity
@Table(
    name = "user_notification_settings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_notification_setting",
            columnNames = {"user_id", "notification_type", "notification_channel"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("설정 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_notification_setting_user"))
    @Comment("사용자")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    @Comment("알림 타입")
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_channel", nullable = false, length = 20)
    @Comment("알림 채널")
    private NotificationChannel notificationChannel;

    @Column(name = "is_agreed", nullable = false)
    @Comment("수신 동의 여부")
    private Boolean isAgreed;

    @Column(name = "is_marketing", nullable = false)
    @Comment("마케팅성 알림 여부")
    private Boolean isMarketing;

    @Column(name = "agreed_at", nullable = true)
    @Comment("동의 일시 (마케팅 알림의 경우 법적 요구사항)")
    private LocalDateTime agreedAt;

    @Column(name = "agreed_at_marketing", nullable = true)
    @Comment("마케팅 알림 동의 일시 (법적 요구사항)")
    private LocalDateTime agreedAtMarketing;

    @Builder
    public UserNotificationSetting(
            User user,
            NotificationType notificationType,
            NotificationChannel notificationChannel,
            Boolean isAgreed,
            Boolean isMarketing,
            LocalDateTime agreedAt,
            LocalDateTime agreedAtMarketing) {
        this.user = user;
        this.notificationType = notificationType;
        this.notificationChannel = notificationChannel;
        this.isAgreed = isAgreed != null ? isAgreed : false;
        this.isMarketing = isMarketing != null ? isMarketing : false;
        this.agreedAt = agreedAt;
        this.agreedAtMarketing = agreedAtMarketing;
    }

    /**
     * 동의 여부 변경
     * 마케팅 알림의 경우 동의 일시를 별도로 추적
     */
    public void updateAgreement(Boolean newAgreement, LocalDateTime agreedAt) {
        boolean previousAgreement = Boolean.TRUE.equals(this.isAgreed);
        boolean newAgreementValue = Boolean.TRUE.equals(newAgreement);

        this.isAgreed = newAgreementValue;
        this.agreedAt = agreedAt;

        // 마케팅 알림의 경우 별도 일시 추적
        if (Boolean.TRUE.equals(this.isMarketing)) {
            if (newAgreementValue && !previousAgreement) {
                // 동의로 변경된 경우: 마케팅 동의 일시 기록
                this.agreedAtMarketing = agreedAt != null ? agreedAt : LocalDateTime.now();
            } else if (!newAgreementValue && previousAgreement) {
                // 비동의로 변경된 경우: 마지막 동의 일시는 유지 (법적 추적 목적)
                // agreedAtMarketing은 유지하여 마지막 동의 일시 기록 보존
            }
        }
    }

    /**
     * 마케팅 알림 동의 여부 확인
     */
    public boolean isMarketingAgreed() {
        return Boolean.TRUE.equals(this.isMarketing) && Boolean.TRUE.equals(this.isAgreed);
    }

    /**
     * 일반 알림 동의 여부 확인
     */
    public boolean isServiceAgreed() {
        return !Boolean.TRUE.equals(this.isMarketing) && Boolean.TRUE.equals(this.isAgreed);
    }
}

