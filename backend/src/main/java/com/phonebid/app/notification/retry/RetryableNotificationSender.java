package com.phonebid.app.notification.retry;

import com.phonebid.app.notification.domain.Notification;
import com.phonebid.app.notification.sender.NotificationSender;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.concurrent.CompletionStage;

/**
 * 알림 발송 실패 시, Resilience4j를 활용한 비동기 재시도 처리
 * 
 * - Thread.sleep 없이 ScheduledExecutorService 기반 비블로킹 재시도
 * - Circuit Breaker로 외부 API 장애 시 빠른 실패
 * - TimeLimiter로 무한 대기 방지
 * - 지수 백오프(Exponential Backoff) 자동 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableNotificationSender {

    private final CircuitBreaker notificationCircuitBreaker;
    private final Retry notificationRetry;
    private final TimeLimiter notificationTimeLimiter;
    private final Executor notificationExecutor;
    
    // 재시도 스케줄링을 위한 별도 스레드 풀 (작은 크기로 충분)
    private final ScheduledExecutorService retryScheduler = 
        Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "retry-scheduler");
            t.setDaemon(true);
            return t;
        });

    /**
     * 알림 발송을 완전 비동기로 실행 (Retry + CircuitBreaker + TimeLimiter 적용)
     * 재시도 시에도 스레드를 블로킹하지 않음
     * 
     * @param sender 알림 발송자
     * @param notification 발송할 알림
     * @return 발송 성공 여부를 담은 CompletableFuture
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendWithRetryAsync(
            NotificationSender sender, 
            Notification notification) {
        
        // 1단계: 기본 비동기 실행
        Supplier<CompletionStage<Boolean>> asyncSupplier = 
            () -> executeAsync(sender, notification);
        
        // 2단계: TimeLimiter 적용 (타임아웃)
        Supplier<CompletionStage<Boolean>> timedSupplier = 
            TimeLimiter.decorateCompletionStage(
                notificationTimeLimiter,
                retryScheduler,
                asyncSupplier
            );
        
        // 3단계: CircuitBreaker 적용
        Supplier<CompletionStage<Boolean>> circuitBreakerSupplier = 
            CircuitBreaker.decorateCompletionStage(
                notificationCircuitBreaker,
                timedSupplier
            );
        
        // 4단계: Retry 적용 (비동기 재시도)
        Supplier<CompletionStage<Boolean>> retriedSupplier = 
            Retry.decorateCompletionStage(
                notificationRetry,
                retryScheduler,
                circuitBreakerSupplier
            );
        
        // 5단계: 실행 및 예외 처리
        return retriedSupplier.get().toCompletableFuture()
            .exceptionally(throwable -> {
                log.error("알림 발송 최종 실패 (Resilience4j): notificationId={}", 
                    notification.getId(), throwable);
                return false;
            });
    }

    /**
     * 실제 발송 로직을 비동기로 실행
     */
    private CompletableFuture<Boolean> executeAsync(
            NotificationSender sender, 
            Notification notification) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean success = sender.send(notification);
                if (!success) {
                    // 실패 시 예외를 던져서 Retry가 작동하도록 함
                    throw new RuntimeException("알림 발송 실패: sender returned false");
                }
                log.debug("알림 발송 성공: notificationId={}", notification.getId());
                return true;
            } catch (Exception e) {
                log.error("알림 발송 중 예외 발생: notificationId={}", notification.getId(), e);
                throw e;
            }
        }, notificationExecutor);
    }

    /**
     * 동기 방식 호출을 위한 래퍼 메서드 (하위 호환성)
     * 내부적으로는 비동기로 처리하되, 결과를 기다려서 반환
     * 
     * @param sender 알림 발송자
     * @param notification 발송할 알림
     * @return 발송 성공 여부
     */
    public boolean sendWithRetry(NotificationSender sender, Notification notification) {
        try {
            return sendWithRetryAsync(sender, notification).get();
        } catch (Exception e) {
            log.error("알림 발송 대기 중 예외 발생: notificationId={}", notification.getId(), e);
            return false;
        }
    }
}
