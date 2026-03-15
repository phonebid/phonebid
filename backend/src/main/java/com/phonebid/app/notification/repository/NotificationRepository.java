package com.phonebid.app.notification.repository;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * 사용자별 알림 목록 조회 (페이징)
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND (n.isDelete = false OR n.isDelete IS NULL) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * 사용자별 미읽음 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.isRead = false " +
           "AND (n.isDelete = false OR n.isDelete IS NULL)")
    long countUnreadByUserId(@Param("userId") UUID userId);

    /**
     * 최근 미읽음 알림 조회 (SSE 초기 전송용)
     * 최근 24시간 내 미읽음 알림을 최대 limit개까지 조회
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.isRead = false " +
           "AND n.createdAt >= :since " +
           "AND (n.isDelete = false OR n.isDelete IS NULL) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentUnreadByUserId(
            @Param("userId") UUID userId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    /**
     * 특정 알림 조회 (사용자 ID로 필터링하여 본인 알림만 조회 가능)
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.id = :notificationId " +
           "AND n.user.id = :userId " +
           "AND (n.isDelete = false OR n.isDelete IS NULL)")
    Optional<Notification> findByIdAndUserId(
            @Param("notificationId") UUID notificationId,
            @Param("userId") UUID userId);

    /**
     * 아카이빙 대상 알림 조회 (90일 경과 알림)
     * 페이징을 통해 메모리 효율적으로 조회
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.createdAt < :cutoffDate " +
           "AND (n.isDelete = false OR n.isDelete IS NULL)")
    Page<Notification> findNotificationsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

    /**
     * 특정 타입의 알림 조회 (알림 그룹화용)
     */
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.type = :type " +
           "AND n.isRead = false " +
           "AND n.createdAt >= :since " +
           "AND (n.isDelete = false OR n.isDelete IS NULL) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserIdAndTypeSince(
            @Param("userId") UUID userId,
            @Param("type") NotificationType type,
            @Param("since") LocalDateTime since);

    // 벌크 업데이트 방식으로 대체됨 (성능 최적화)
    // /**
    //  * 사용자별 모든 알림 조회 (소프트 삭제 제외)
    //  * 일괄 읽음 처리 및 일괄 삭제용
    //  */
    // @Query("SELECT n FROM Notification n " +
    //        "WHERE n.user.id = :userId " +
    //        "AND (n.isDelete = false OR n.isDelete IS NULL)")
    // List<Notification> findAllByUserId(@Param("userId") UUID userId);

    // 벌크 업데이트 방식으로 대체됨 (성능 최적화)
    // /**
    //  * 사용자별 미읽음 알림 조회 (일괄 읽음 처리용)
    //  */
    // @Query("SELECT n FROM Notification n " +
    //        "WHERE n.user.id = :userId " +
    //        "AND n.isRead = false " +
    //        "AND (n.isDelete = false OR n.isDelete IS NULL)")
    // List<Notification> findUnreadByUserId(@Param("userId") UUID userId);

    /**
     * 사용자별 모든 미읽음 알림 일괄 읽음 처리 (벌크 업데이트)
     * 
     * @param userId 사용자 ID
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n " +
           "SET n.isRead = true " +
           "WHERE n.user.id = :userId " +
           "AND n.isRead = false " +
           "AND (n.isDelete = false OR n.isDelete IS NULL)")
    int markAllAsReadByUserId(@Param("userId") UUID userId);

    /**
     * 사용자별 모든 알림 일괄 소프트 삭제 (벌크 업데이트)
     * 
     * @param userId 사용자 ID
     * @param deletedBy 삭제자
     * @return 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n " +
           "SET n.isDelete = true, " +
           "n.deletedAt = CURRENT_TIMESTAMP, " +
           "n.deletedBy = :deletedBy " +
           "WHERE n.user.id = :userId " +
           "AND (n.isDelete = false OR n.isDelete IS NULL)")
    int softDeleteAllByUserId(@Param("userId") UUID userId,
                              @Param("deletedBy") String deletedBy);
}

