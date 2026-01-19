package com.phonebid.app.s3.scheduler;

import com.phonebid.app.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 임시 파일 정리 스케줄러
 * 판매자 회원가입 중간 이탈 시 남아있는 temp/ 폴더의 임시 파일을 정기적으로 정리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupScheduler {

    private final S3Service s3Service;

    // 스케줄러 설정
    private static final boolean ENABLED = true;
    private static final String TEMP_PREFIX = "temp/seller-documents/";
    private static final long OLDER_THAN_HOURS = 24;

    /**
     * 임시 파일 정리 작업
     * 매일 새벽 3시에 실행 (cron: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupTempFiles() {
        if (!ENABLED) {
            log.debug("임시 파일 정리 스케줄러가 비활성화되어 있습니다.");
            return;
        }

        log.info("임시 파일 정리 작업 시작: prefix={}, olderThanHours={}", TEMP_PREFIX, OLDER_THAN_HOURS);

        try {
            int deletedCount = s3Service.deleteOldTempFiles(TEMP_PREFIX, OLDER_THAN_HOURS);
            log.info("임시 파일 정리 작업 완료: 삭제된 파일 수={}", deletedCount);
        } catch (Exception e) {
            log.error("임시 파일 정리 작업 중 오류 발생", e);
        }
    }
}

