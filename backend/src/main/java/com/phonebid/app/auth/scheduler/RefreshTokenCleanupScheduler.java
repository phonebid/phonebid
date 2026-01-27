package com.phonebid.app.auth.scheduler;

import com.phonebid.app.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * RefreshToken 정리 스케줄러
 * - 만료된 RefreshToken soft delete
 * - 삭제된 지 오래된 RefreshToken 하드 삭제 (영구 삭제)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    /**
     * 삭제 후 경과 기간 (월) - 3개월 이상 지난 삭제된 토큰을 하드 삭제
     */
    private static final int HARD_DELETE_MONTHS = 3;

    /**
     * 만료된 RefreshToken 정리 작업
     * 매일 자정에 실행 (cron: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        log.info("만료된 RefreshToken 정리 작업 시작");
        
        try {
            refreshTokenService.deleteExpiredTokens();
            log.info("만료된 RefreshToken 정리 작업 완료");
        } catch (Exception e) {
            log.error("만료된 RefreshToken 정리 작업 중 오류 발생", e);
        }
    }

    /**
     * 삭제된 지 오래된 RefreshToken 하드 삭제 작업 (영구 삭제)
     * 매주 일요일 새벽 2시에 실행 (cron: 초 분 시 일 월 요일)
     * deleted_at이 3개월 이상 지난 데이터를 실제 DB에서 영구 삭제하여 용량 관리
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void hardDeleteOldDeletedTokens() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("=== 삭제된 RefreshToken 하드 삭제 배치 작업 시작 ===");
        log.info("기준: 삭제된 지 {}개월 이상 지난 데이터", HARD_DELETE_MONTHS);

        try {
            int deletedCount = refreshTokenService.hardDeleteOldDeletedTokens(HARD_DELETE_MONTHS);
            
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            log.info("=== 삭제된 RefreshToken 하드 삭제 배치 작업 완료 ===");
            log.info("실행 시간: {}초", duration.getSeconds());
            log.info("삭제된 레코드 수: {}개", deletedCount);
        } catch (Exception e) {
            log.error("삭제된 RefreshToken 하드 삭제 배치 작업 중 오류 발생", e);
        }
    }
}

