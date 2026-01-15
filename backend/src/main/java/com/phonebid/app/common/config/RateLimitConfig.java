package com.phonebid.app.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate Limiting을 위한 Bucket 생성 및 관리 설정 클래스
 * Caffeine Cache를 사용하여 IP별 Bucket을 인메모리로 관리합니다.
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit.temp-upload")
@Data
public class RateLimitConfig {

    /**
     * 분당 허용 요청 수 (기본값: 10)
     */
    private int perMinute = 10;

    /**
     * 시간당 허용 요청 수 (기본값: 30)
     */
    private int perHour = 30;

    /**
     * IP별 Bucket을 저장하는 Caffeine Cache
     * - 최대 10,000개의 IP 주소 저장
     * - 2시간 동안 접근이 없으면 자동으로 만료
     */
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    /**
     * IP 주소를 기반으로 Bucket을 생성하거나 반환합니다.
     * 
     * @param key 캐시 키 (일반적으로 IP 주소)
     * @return Bucket 인스턴스
     */
    public Bucket resolveBucket(String key) {
        return bucketCache.get(key, k -> createNewBucket());
    }

    /**
     * 새로운 Bucket을 생성합니다.
     * 분당 제한과 시간당 제한을 모두 적용합니다.
     * 
     * @return 새로 생성된 Bucket 인스턴스
     */
    private Bucket createNewBucket() {
        // 분당 제한
        Bandwidth minuteLimit = Bandwidth.builder()
                .capacity(perMinute)
                .refillGreedy(perMinute, Duration.ofMinutes(1))
                .build();

        // 시간당 제한
        Bandwidth hourLimit = Bandwidth.builder()
                .capacity(perHour)
                .refillGreedy(perHour, Duration.ofHours(1))
                .build();

        // 두 제한을 모두 적용 (더 엄격한 제한이 적용됨)
        return Bucket.builder()
                .addLimit(minuteLimit)
                .addLimit(hourLimit)
                .build();
    }
}

