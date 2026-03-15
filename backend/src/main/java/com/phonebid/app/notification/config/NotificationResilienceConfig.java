package com.phonebid.app.notification.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 알림 발송을 위한 Resilience4j 설정
 * Circuit Breaker, Retry, TimeLimiter를 조합하여 외부 API 호출의 안정성 확보
 * Spring Boot 자동 구성으로 생성된 레지스트리를 사용하여 application.yml 설정 반영
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationResilienceConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    /**
     * 이벤트 리스너 등록
     * application.yml의 설정이 적용된 레지스트리에 로깅 이벤트 리스너를 추가
     */
    @PostConstruct
    public void registerEventListeners() {
        // CircuitBreaker 이벤트 리스너 등록
        circuitBreakerRegistry.circuitBreaker("notification").getEventPublisher()
            .onStateTransition(event -> {
                log.warn("Circuit Breaker 상태 변경: {} -> {}", 
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState());
            })
            .onError(event -> {
                log.error("Circuit Breaker 에러 발생: {}", event.getThrowable().getMessage());
            });
        
        // Retry 이벤트 리스너 등록
        retryRegistry.retry("notification").getEventPublisher()
            .onRetry(event -> {
                log.warn("알림 발송 재시도: attempt={}, lastException={}", 
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable().getMessage());
            })
            .onError(event -> {
                log.error("알림 발송 최종 실패 (모든 재시도 소진): attempts={}", 
                    event.getNumberOfRetryAttempts());
            });
    }

    /**
     * 알림 발송용 CircuitBreaker 인스턴스
     * Spring Boot 자동 구성으로 생성된 레지스트리에서 가져옴
     */
    @Bean
    public CircuitBreaker notificationCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("notification");
    }

    /**
     * 알림 발송용 Retry 인스턴스
     * Spring Boot 자동 구성으로 생성된 레지스트리에서 가져옴
     */
    @Bean
    public Retry notificationRetry(RetryRegistry registry) {
        return registry.retry("notification");
    }

    /**
     * 알림 발송용 TimeLimiter 인스턴스
     * Spring Boot 자동 구성으로 생성된 레지스트리에서 가져옴
     */
    @Bean
    public TimeLimiter notificationTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("notification");
    }
}
