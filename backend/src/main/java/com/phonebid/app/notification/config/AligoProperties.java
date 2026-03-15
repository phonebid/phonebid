package com.phonebid.app.notification.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 알리고(Aligo) 카카오 알림톡 API 설정
 * application.yml의 aligo.* 속성을 바인딩
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aligo")
public class AligoProperties {
    
    @Valid
    @NotNull
    private Api api = new Api();
    
    @Valid
    @NotNull
    private Sender sender = new Sender();
    
    @Valid
    @NotNull
    private Template template = new Template();
    
    @Valid
    private Timeout timeout = new Timeout();
    
    @Valid
    @NotNull
    private Retry retry = new Retry();
    
    @Valid
    @NotNull
    private ConnectionPool connectionPool = new ConnectionPool();
    
    /**
     * API 기본 설정
     */
    @Getter
    @Setter
    public static class Api {
        @NotBlank(message = "알리고 Base URL은 필수입니다")
        private String baseUrl;
        
        @NotBlank(message = "알리고 User ID는 필수입니다")
        private String userId;
        
        @NotBlank(message = "알리고 API Key는 필수입니다")
        private String apiKey;
        
        @AssertTrue(message = "Base URL은 HTTPS를 사용해야 합니다")
        private boolean isSecureUrl() {
            return baseUrl != null && baseUrl.toLowerCase().startsWith("https://");
        }
    }
    
    /**
     * 발신자 설정
     */
    @Getter
    @Setter
    public static class Sender {
        @NotBlank(message = "발신 프로필 키는 필수입니다")
        private String key;
        
        private String phone;
    }
    
    /**
     * 템플릿 코드 설정
     */
    @Getter
    @Setter
    public static class Template {
        private String bidArrived;
        private String bidSelected;
        private String contractSigned;
        private String paymentCompleted;
        private String deliveryStarted;
        private String deliveryCompleted;
    }
    
    /**
     * 타임아웃 설정
     */
    @Getter
    @Setter
    public static class Timeout {
        @NotNull(message = "Connect timeout은 필수입니다")
        @DurationMin(millis = 1, message = "Connect timeout은 0보다 커야 합니다")
        @DurationMax(seconds = 300, message = "Connect timeout은 최대 300초입니다")
        private Duration connect = Duration.ofSeconds(5);
        
        @NotNull(message = "Read timeout은 필수입니다")
        @DurationMin(millis = 1, message = "Read timeout은 0보다 커야 합니다")
        @DurationMax(seconds = 600, message = "Read timeout은 최대 600초입니다")
        private Duration read = Duration.ofSeconds(10);
        
        @NotNull(message = "Write timeout은 필수입니다")
        @DurationMin(millis = 1, message = "Write timeout은 0보다 커야 합니다")
        @DurationMax(seconds = 600, message = "Write timeout은 최대 600초입니다")
        private Duration write = Duration.ofSeconds(10);
    }
    
    /**
     * 재시도 설정
     */
    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private Duration backoffDelay = Duration.ofSeconds(1);
    }
    
    /**
     * Connection Pool 설정
     */
    @Getter
    @Setter
    public static class ConnectionPool {
        @Positive(message = "최대 연결 수는 양수여야 합니다")
        private int maxConnections = 50;
        
        @NotNull(message = "연결 대기 타임아웃은 필수입니다")
        @DurationMin(millis = 1, message = "연결 대기 타임아웃은 0보다 커야 합니다")
        @DurationMax(seconds = 300, message = "연결 대기 타임아웃은 최대 300초입니다")
        private Duration pendingAcquireTimeout = Duration.ofSeconds(30);
    }
}
