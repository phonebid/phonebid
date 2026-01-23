package com.phonebid.app.auth.scheduler;

import com.phonebid.app.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 RefreshToken을 정리하는 스케줄러
 * 매일 자정에 실행되어 만료된 토큰을 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

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
}

