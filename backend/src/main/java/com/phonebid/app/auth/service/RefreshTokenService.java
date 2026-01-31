package com.phonebid.app.auth.service;

import com.phonebid.app.auth.domain.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * RefreshToken 저장 및 조회를 위한 서비스 인터페이스
 * 추상화를 통해 향후 RDB에서 타 DB(예: Redis)로 쉽게 교체 가능하도록 설계
 */
public interface RefreshTokenService {

    /**
     * RefreshToken 생성 및 저장
     * @param userId 사용자 ID
     * @return 생성된 RefreshToken 값
     */
    String createRefreshToken(UUID userId);

    /**
     * 토큰 값으로 RefreshToken 조회
     * @param token RefreshToken 값
     * @return RefreshToken 엔티티 (없으면 Optional.empty())
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 RefreshToken 조회
     * @param userId 사용자 ID
     * @return RefreshToken 엔티티 (없으면 Optional.empty())
     */
    Optional<RefreshToken> findByUserId(UUID userId);

    /**
     * 사용자 ID로 RefreshToken 삭제
     * @param userId 사용자 ID
     */
    void deleteByUserId(UUID userId);

    /**
     * 만료된 RefreshToken 삭제
     */
    void deleteExpiredTokens();

    /**
     * 삭제된 지 일정 기간 이상 지난 RefreshToken 하드 삭제 (영구 삭제)
     * 배치 작업에서 사용
     * @param months 삭제 후 경과 기간 (월)
     * @return 삭제된 레코드 수
     */
    int hardDeleteOldDeletedTokens(int months);

    /**
     * RefreshToken 유효성 검증
     * @param token RefreshToken 값
     * @return 유효하면 true, 그렇지 않으면 false
     */
    boolean validateToken(String token);

    /**
     * 사용자명으로 RefreshToken 값 조회
     * @param username 사용자명
     * @return RefreshToken 값 (해시되지 않은 원본 토큰)
     * @throws CustomException 사용자를 찾을 수 없거나 RefreshToken을 찾을 수 없는 경우
     */
    String getTokenByUsername(String username);
}

