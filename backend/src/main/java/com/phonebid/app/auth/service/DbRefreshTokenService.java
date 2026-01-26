package com.phonebid.app.auth.service;

import com.phonebid.app.auth.domain.RefreshToken;
import com.phonebid.app.auth.repository.RefreshTokenRepository;
import com.phonebid.app.auth.util.TokenHashUtil;
import com.phonebid.app.common.Constants;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * RDB 기반 RefreshTokenService 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbRefreshTokenService implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenHashUtil tokenHashUtil;

    @Override
    @Transactional
    public String createRefreshToken(UUID userId) {
        // 사용자 조회 (삭제 전에 조회하여 영속성 컨텍스트에 로드)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
        
        // 기존 RefreshToken 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // RefreshToken 생성
        String token = jwtUtil.createRefreshToken(user.getUsername());
        String hashedToken = tokenHashUtil.hashToken(token);
        LocalDateTime expiresAt = LocalDateTime.now().plus(Constants.Jwt.REFRESH_TOKEN_EXPIRY);

        // DB 저장 (해시된 토큰만 저장)
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(hashedToken)
            .expiresAt(expiresAt)
            .build();

        refreshTokenRepository.save(refreshToken);
        log.info("RefreshToken 생성 완료: userId={}", userId);

        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        String hashedToken = tokenHashUtil.hashToken(token);
        return refreshTokenRepository.findByToken(hashedToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUserId(UUID userId) {
        return refreshTokenRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("RefreshToken 삭제 완료: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteByExpiresAtBefore(now);
        log.info("만료된 RefreshToken 정리 완료: 기준 시각={}", now);
    }

    @Override
    public boolean validateToken(String token) {
        // JWT 검증
        if (!jwtUtil.validateRefreshToken(token)) {
            return false;
        }

        // 토큰을 해시화하여 DB에서 조회
        String hashedToken = tokenHashUtil.hashToken(token);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(hashedToken);
        if (refreshTokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // 만료 시간 확인
        if (refreshToken.isExpired()) {
            return false;
        }

        return true;
    }
}

