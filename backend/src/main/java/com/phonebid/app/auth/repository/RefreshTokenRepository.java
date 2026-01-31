package com.phonebid.app.auth.repository;

import com.phonebid.app.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * 토큰 값으로 RefreshToken 조회 (삭제되지 않은 것만)
     * BaseTimeEntity의 @SQLRestriction으로 자동 필터링되지만 명시적으로 표시
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.deletedAt IS NULL")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    /**
     * 사용자 ID로 RefreshToken 조회 (삭제되지 않은 것만)
     * BaseTimeEntity의 @SQLRestriction으로 자동 필터링되지만 명시적으로 표시
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deletedAt IS NULL")
    Optional<RefreshToken> findByUserId(@Param("userId") UUID userId);

    /**
     * 사용자 ID로 RefreshToken soft delete
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.deletedAt = :deletedAt WHERE rt.user.id = :userId AND rt.deletedAt IS NULL")
    void deleteByUserId(@Param("userId") UUID userId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 만료된 RefreshToken soft delete
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.deletedAt = :deletedAt WHERE rt.expiresAt < :dateTime AND rt.deletedAt IS NULL")
    void deleteByExpiresAtBefore(@Param("dateTime") LocalDateTime dateTime, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 삭제된 지 일정 기간 이상 지난 RefreshToken 하드 삭제 (영구 삭제)
     * 배치 작업에서 사용
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.deletedAt IS NOT NULL AND rt.deletedAt < :cutoffDate")
    int hardDeleteByDeletedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}

