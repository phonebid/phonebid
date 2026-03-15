package com.phonebid.app.notification.repository;

import com.phonebid.app.notification.domain.NotificationChannel;
import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.notification.domain.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 알림 설정 Repository
 */
@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, UUID> {

    /**
     * 사용자, 알림 타입, 채널로 설정 조회
     */
    @Query("SELECT s FROM UserNotificationSetting s " +
           "WHERE s.user.id = :userId " +
           "AND s.notificationType = :type " +
           "AND s.notificationChannel = :channel " +
           "AND s.isDelete = false")
    Optional<UserNotificationSetting> findByUserIdAndTypeAndChannel(
            @Param("userId") UUID userId,
            @Param("type") NotificationType type,
            @Param("channel") NotificationChannel channel
    );

    /**
     * 사용자의 특정 채널에 대한 모든 설정 조회
     */
    @Query("SELECT s FROM UserNotificationSetting s " +
           "WHERE s.user.id = :userId " +
           "AND s.notificationChannel = :channel " +
           "AND s.isDelete = false")
    List<UserNotificationSetting> findByUserIdAndChannel(
            @Param("userId") UUID userId,
            @Param("channel") NotificationChannel channel
    );

    /**
     * 사용자의 모든 알림 설정 조회
     */
    @Query("SELECT s FROM UserNotificationSetting s " +
           "WHERE s.user.id = :userId " +
           "AND s.isDelete = false")
    List<UserNotificationSetting> findByUserId(@Param("userId") UUID userId);

    /**
     * 사용자의 특정 알림 타입에 대한 설정 조회
     */
    @Query("SELECT s FROM UserNotificationSetting s " +
           "WHERE s.user.id = :userId " +
           "AND s.notificationType = :type " +
           "AND s.isDelete = false")
    List<UserNotificationSetting> findByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") NotificationType type
    );

    /**
     * 사용자의 마케팅 알림 동의 설정 조회
     */
    @Query("SELECT s FROM UserNotificationSetting s " +
           "WHERE s.user.id = :userId " +
           "AND s.isMarketing = true " +
           "AND s.isAgreed = true " +
           "AND s.isDelete = false")
    List<UserNotificationSetting> findMarketingAgreedSettingsByUserId(@Param("userId") UUID userId);
}

