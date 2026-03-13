package com.phonebid.app.notification.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 알림 발송을 위한 Resilience4j 설정
 * Circuit Breaker, Retry, TimeLimiter를 조합하여 외부 API 호출의 안정성 확보
 */
@Slf4j
@Configuration
public class NotificationResilienceConfig {

    /**
     * CircuitBreaker Registry Bean
     * application.yml의 설정을 기반으로 CircuitBreaker 인스턴스 관리
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        
        // 이벤트 리스너 등록 - 상태 변경 시 로깅
        registry.circuitBreaker("notification").getEventPublisher()
            .onStateTransition(event -> {
                log.warn("Circuit Breaker 상태 변경: {} -> {}", 
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState());
            })
            .onError(event -> {
                log.error("Circuit Breaker 에러 발생: {}", event.getThrowable().getMessage());
            });
        
        return registry;
    }

    /**
     * Retry Registry Bean
     * application.yml의 설정을 기반으로 Retry 인스턴스 관리
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();
        
        // 이벤트 리스너 등록 - 재시도 발생 시 로깅
        registry.retry("notification").getEventPublisher()
            .onRetry(event -> {
                log.warn("알림 발송 재시도: attempt={}, lastException={}", 
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable().getMessage());
            })
            .onError(event -> {
                log.error("알림 발송 최종 실패 (모든 재시도 소진): attempts={}", 
                    event.getNumberOfRetryAttempts());
            });
        
        return registry;
    }

    /**
     * TimeLimiter Registry Bean
     * application.yml의 설정을 기반으로 TimeLimiter 인스턴스 관리
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        return TimeLimiterRegistry.ofDefaults();
    }

    /**
     * 알림 발송용 CircuitBreaker 인스턴스
     */
    @Bean
    public CircuitBreaker notificationCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("notification");
    }

    /**
     * 알림 발송용 Retry 인스턴스
     */
    @Bean
    public Retry notificationRetry(RetryRegistry registry) {
        return registry.retry("notification");
    }

    /**
     * 알림 발송용 TimeLimiter 인스턴스
     */
    @Bean
    public TimeLimiter notificationTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("notification");
    }
}
