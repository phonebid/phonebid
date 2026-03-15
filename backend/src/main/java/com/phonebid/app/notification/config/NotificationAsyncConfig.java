package com.phonebid.app.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 알림 비동기 처리 설정
 * 별도 쓰레드 풀을 생성하여 알림 발송이 다른 비즈니스 로직에 영향을 주지 않도록 분리
 */
@Slf4j
@Configuration
@EnableAsync
public class NotificationAsyncConfig {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final String THREAD_NAME_PREFIX = "notification-async-";

    /**
     * 알림 전용 쓰레드 풀 Executor Bean 정의
     * 
     * @return notificationExecutor
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("알림 전용 쓰레드 풀 생성 완료: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        
        return executor;
    }
}

