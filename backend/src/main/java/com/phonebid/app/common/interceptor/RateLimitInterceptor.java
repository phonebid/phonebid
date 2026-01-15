package com.phonebid.app.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.common.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting을 적용하는 인터셉터
 * 컨트롤러에 도달하기 전에 요청 횟수를 제한하여
 * 파일 업로드 등의 리소스 소모를 방지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 컨트롤러 실행 전에 Rate Limit을 검사합니다.
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler Handler
     * @return true면 요청 계속 진행, false면 요청 차단
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                            @NonNull HttpServletResponse response,
                            @NonNull Object handler) throws Exception {

        // 특정 엔드포인트만 체크
        String uri = request.getRequestURI();
        if (!uri.equals("/api/v1/sellers/documents/temp")) {
            return true;
        }

        String clientIp = getClientIP(request);
        String key = "temp-upload:" + clientIp;

        Bucket bucket = rateLimitConfig.resolveBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // 남은 횟수를 헤더에 추가
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        // Rate Limit 초과
        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(
                probe.getNanosToWaitForRefill()
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(waitSeconds));

        Map<String, Object> error = Map.of(
                "error", "RATE_LIMIT_EXCEEDED",
                "message", "파일 업로드 제한 횟수를 초과했습니다.",
                "retryAfterSeconds", waitSeconds,
                "detail", String.format("약 %d초 후 다시 시도해주세요.", waitSeconds)
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));

        log.warn("Rate limit exceeded - IP: {}, URI: {}, Wait: {}s", clientIp, uri, waitSeconds);
        return false;
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출합니다.
     * 프록시나 로드밸런서를 통한 요청도 고려합니다.
     * 
     * @param request HttpServletRequest
     * @return 클라이언트 IP 주소
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
            // X-Forwarded-For는 여러 IP를 포함할 수 있으므로 첫 번째 IP를 사용
            return xfHeader.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

