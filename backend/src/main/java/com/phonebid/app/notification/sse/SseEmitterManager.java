package com.phonebid.app.notification.sse;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 연결 생명주기 관리 클래스
 * 사용자별 SseEmitter를 저장하고 관리하며, 메모리 누수 방지를 위한 정리 작업 수행
 * 
 * 주요 기능:
 * - 원자적 연산을 통한 동시성 안정성 보장
 * - 연결 확립 즉시 확인 이벤트 전송 (인프라 타임아웃 방지)
 * - 주기적 Heartbeat 전송으로 유령 연결 감지 및 제거
 * - 정기 Cleanup 스케줄러로 타임아웃된 연결 강제 정리
 * - Graceful Shutdown 지원
 */
@Slf4j
@Component
public class SseEmitterManager {

    /**
     * SSE 연결 타임아웃 시간 (기본값: 30분)
     */
    @Value("${sse.timeout:30m}")
    private Duration sseTimeout;

    /**
     * Heartbeat 전송 간격 (기본값: 30초)
     */
    @Value("${sse.heartbeat.interval:30s}")
    private Duration heartbeatInterval;

    /**
     * Heartbeat 활성화 여부 (기본값: true)
     */
    @Value("${sse.heartbeat.enabled:true}")
    private boolean heartbeatEnabled;

    /**
     * Cleanup 스케줄러 활성화 여부 (기본값: true)
     */
    @Value("${sse.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * Cleanup 스케줄러 실행 간격 (기본값: 10분)
     */
    @Value("${sse.cleanup.interval:10m}")
    private Duration cleanupInterval;

    /**
     * 최대 연결 수 제한 (기본값: 10000, 0이면 제한 없음)
     */
    @Value("${sse.max-connections:10000}")
    private int maxConnections;

    /**
     * 사용자별 최대 중복 연결 수 (기본값: 3)
     */
    @Value("${sse.max-duplicate-connections-per-user:3}")
    private int maxDuplicateConnectionsPerUser;

    /**
     * SSE 연결 정보를 담는 내부 클래스
     */
    private static class ConnectionInfo {
        final SseEmitter emitter;
        final Instant createdAt;

        ConnectionInfo(SseEmitter emitter) {
            this.emitter = emitter;
            this.createdAt = Instant.now();
        }
    }

    /**
     * 사용자별 SseEmitter 저장소
     * Key: userId, Value: ConnectionInfo (emitter + 생성 시간)
     */
    private final Map<UUID, ConnectionInfo> connections = new ConcurrentHashMap<>();

    /**
     * 새로운 SSE 연결 생성 및 등록
     * 
     * 동시성 안정성: compute()를 사용하여 원자적 연산 보장
     * 연결 Handshake: 저장 직후 즉시 "connected" 이벤트 전송
     * 
     * @param userId 사용자 ID
     * @return 생성된 SseEmitter
     * @throws RuntimeException 연결 수 제한 초과 또는 Handshake 실패 시
     */
    public SseEmitter createConnection(UUID userId) {
        // 최대 연결 수 제한 확인
        if (maxConnections > 0 && connections.size() >= maxConnections) {
            log.warn("SSE 연결 수 제한 초과: 현재={}, 최대={}", connections.size(), maxConnections);
            throw new RuntimeException("SSE 연결 수가 최대치에 도달했습니다. 잠시 후 다시 시도해주세요.");
        }

        SseEmitter emitter = new SseEmitter(sseTimeout.toMillis());

        // 타임아웃 콜백: 연결이 타임아웃되면 저장소에서 제거
        emitter.onTimeout(() -> {
            try {
                log.debug("SSE 연결 타임아웃: userId={}", userId);
            } finally {
                // 어떤 경우에도 제거 작업이 실행되도록 finally에서 처리
                removeConnection(userId);
            }
        });

        // 완료 콜백: 연결이 정상적으로 완료되면 저장소에서 제거
        emitter.onCompletion(() -> {
            try {
                log.debug("SSE 연결 완료: userId={}", userId);
            } finally {
                removeConnection(userId);
            }
        });

        // 에러 콜백: 에러 발생 시 저장소에서 제거
        emitter.onError((ex) -> {
            try {
                log.error("SSE 연결 에러: userId={}, error={}", userId, ex.getMessage(), ex);
            } finally {
                removeConnection(userId);
            }
        });

        // 원자적 연산: compute()를 사용하여 기존 연결 제거와 새 연결 등록을 한 번에 처리
        connections.compute(userId, (key, existing) -> {
            // 기존 연결이 있으면 종료
            if (existing != null) {
                try {
                    existing.emitter.complete();
                    log.debug("기존 SSE 연결 종료: userId={}", userId);
                } catch (Exception e) {
                    log.warn("기존 SSE 연결 종료 중 에러: userId={}", userId, e);
                }
            }
            // 새 연결 정보 반환
            return new ConnectionInfo(emitter);
        });

        log.info("SSE 연결 생성: userId={}, 현재 연결 수={}", userId, connections.size());

        // 연결 확립 즉시 확인 이벤트 전송 (Handshake)
        // 인프라 계층(Nginx, ALB) 타임아웃 방지를 위해 필수
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결이 성공적으로 확립되었습니다."));
            log.debug("SSE 연결 Handshake 완료: userId={}", userId);
        } catch (IOException e) {
            // Handshake 실패 시 즉시 연결 제거 및 예외 발생
            log.error("SSE 연결 Handshake 실패: userId={}, error={}", userId, e.getMessage(), e);
            removeConnection(userId);
            throw new RuntimeException("SSE 연결 확립에 실패했습니다. 재연결을 시도해주세요.", e);
        }

        return emitter;
    }

    /**
     * 특정 사용자의 SseEmitter 조회
     * 
     * @param userId 사용자 ID
     * @return SseEmitter (없으면 null)
     */
    public SseEmitter getConnection(UUID userId) {
        ConnectionInfo info = connections.get(userId);
        return info != null ? info.emitter : null;
    }

    /**
     * SSE 연결 제거 (메모리 누수 방지)
     * 
     * @param userId 사용자 ID
     */
    public void removeConnection(UUID userId) {
        ConnectionInfo removed = connections.remove(userId);
        if (removed != null) {
            log.debug("SSE 연결 제거: userId={}", userId);
        }
    }

    /**
     * 모든 연결 제거 (서버 종료 시 등)
     */
    public void removeAllConnections() {
        log.info("모든 SSE 연결 제거 시작: 총 {}개", connections.size());
        connections.forEach((userId, info) -> {
            try {
                info.emitter.complete();
            } catch (Exception e) {
                log.warn("SSE 연결 종료 중 에러: userId={}", userId, e);
            }
        });
        connections.clear();
        log.info("모든 SSE 연결 제거 완료");
    }

    /**
     * 현재 활성 연결 수 조회
     * 
     * @return 활성 연결 수
     */
    public int getActiveConnectionCount() {
        return connections.size();
    }

    /**
     * 특정 사용자가 연결되어 있는지 확인
     * 
     * @param userId 사용자 ID
     * @return 연결 여부
     */
    public boolean isConnected(UUID userId) {
        return connections.containsKey(userId);
    }

    /**
     * Heartbeat 전송 간격 조회
     * 
     * @return Heartbeat 간격 (밀리초)
     */
    public long getHeartbeatInterval() {
        return heartbeatInterval.toMillis();
    }

    /**
     * 실시간 Heartbeat 스케줄러
     * 모든 활성 연결에 주기적으로 ping 이벤트를 전송하여 유령 연결을 감지하고 제거
     * 
     * 주의: forEach 내부에서 직접 remove()를 호출하면 ConcurrentModificationException 발생 가능
     * 따라서 Iterator를 사용하거나 별도 제거 리스트를 활용하여 안전하게 처리
     */
    @Scheduled(fixedDelayString = "${sse.heartbeat.interval:30000}", initialDelay = 30000)
    public void sendHeartbeat() {
        if (!heartbeatEnabled) {
            return;
        }

        int totalConnections = connections.size();
        if (totalConnections == 0) {
            return;
        }

        int successCount = 0;
        int failureCount = 0;
        // 제거할 연결 목록 (Iterator 사용으로 안전한 제거)
        Iterator<Map.Entry<UUID, ConnectionInfo>> iterator = connections.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ConnectionInfo> entry = iterator.next();
            UUID userId = entry.getKey();
            ConnectionInfo info = entry.getValue();

            try {
                // comment("") 또는 ping 이벤트 전송
                info.emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("heartbeat"));
                successCount++;
            } catch (IOException e) {
                // 전송 실패 시 해당 연결 제거
                log.warn("SSE Heartbeat 전송 실패, 연결 제거: userId={}, error={}", 
                        userId, e.getMessage());
                try {
                    info.emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.debug("SSE 연결 에러 완료 처리 중 예외: userId={}", userId, ex);
                }
                iterator.remove(); // 안전한 제거
                failureCount++;
            } catch (Exception e) {
                // 예상치 못한 예외 처리
                log.error("SSE Heartbeat 전송 중 예상치 못한 에러: userId={}, error={}", 
                        userId, e.getMessage(), e);
                try {
                    info.emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.debug("SSE 연결 에러 완료 처리 중 예외: userId={}", userId, ex);
                }
                iterator.remove();
                failureCount++;
            }
        }

        if (failureCount > 0 || log.isDebugEnabled()) {
            log.debug("SSE Heartbeat 전송 완료: 전체={}, 성공={}, 실패={}", 
                    totalConnections, successCount, failureCount);
        }
    }

    /**
     * 정기 Cleanup 스케줄러
     * 콜백이 누락될 경우를 대비해 타임아웃된 연결을 강제로 정리
     * 
     * 생성 시간을 체크하여 타임아웃 시간을 초과한 연결을 제거
     */
    @Scheduled(fixedDelayString = "${sse.cleanup.interval:600000}", initialDelay = 600000)
    public void cleanupStaleConnections() {
        if (!cleanupEnabled) {
            return;
        }

        Instant now = Instant.now();
        long timeoutMillis = sseTimeout.toMillis();
        int removedCount = 0;

        Iterator<Map.Entry<UUID, ConnectionInfo>> iterator = connections.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ConnectionInfo> entry = iterator.next();
            UUID userId = entry.getKey();
            ConnectionInfo info = entry.getValue();

            // 생성 시간으로부터 경과 시간 계산
            long elapsedMillis = Duration.between(info.createdAt, now).toMillis();

            if (elapsedMillis > timeoutMillis) {
                log.warn("SSE 연결 타임아웃 감지 (Cleanup): userId={}, 경과시간={}ms", 
                        userId, elapsedMillis);
                try {
                    info.emitter.complete();
                } catch (Exception e) {
                    log.debug("SSE 연결 종료 중 에러: userId={}", userId, e);
                }
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("SSE Cleanup 완료: 제거된 연결={}개, 남은 연결={}개", 
                    removedCount, connections.size());
        }
    }

    /**
     * Graceful Shutdown
     * 서버 종료 시 모든 연결을 안전하게 닫음
     */
    @PreDestroy
    public void shutdown() {
        log.info("SSE Emitter Manager 종료 시작");
        removeAllConnections();
        log.info("SSE Emitter Manager 종료 완료");
    }
}
