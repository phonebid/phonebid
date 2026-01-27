package com.phonebid.app.common.util;

import com.phonebid.app.common.Constants;
import com.phonebid.app.jwt.JwtUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;

/**
 * 쿠키 생성 유틸리티 클래스
 * Access Token 및 Refresh Token 쿠키 생성을 공통화
 */
public class CookieUtil {

    /**
     * Access Token 쿠키 생성
     * @param accessToken Access Token 값 (Bearer 접두사 포함 가능)
     * @param isProduction 프로덕션 환경 여부
     * @param maxAge 쿠키 만료 시간
     * @return ResponseCookie
     */
    public static ResponseCookie createAccessTokenCookie(String accessToken, boolean isProduction, Duration maxAge) {
        // Bearer 접두사 제거
        String accessTokenValue = accessToken.startsWith(JwtUtil.BEARER_PREFIX)
            ? accessToken.substring(JwtUtil.BEARER_PREFIX.length())
            : accessToken;

        return ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, accessTokenValue)
                .path("/")
                .httpOnly(true) // XSS 공격 방지
                .secure(isProduction) // 프로덕션에서만 HTTPS 필수
                .sameSite("Strict") // CSRF 공격 방지
                .maxAge(maxAge)
                .build();
    }

    /**
     * Access Token 쿠키 생성 (기본 만료 시간: 1시간)
     * @param accessToken Access Token 값 (Bearer 접두사 포함 가능)
     * @param isProduction 프로덕션 환경 여부
     * @return ResponseCookie
     */
    public static ResponseCookie createAccessTokenCookie(String accessToken, boolean isProduction) {
        return createAccessTokenCookie(accessToken, isProduction, Constants.Jwt.DEFAULT_EXPIRY);
    }

    /**
     * Refresh Token 쿠키 생성
     * @param refreshToken Refresh Token 값
     * @param isProduction 프로덕션 환경 여부
     * @return ResponseCookie
     */
    public static ResponseCookie createRefreshTokenCookie(String refreshToken, boolean isProduction) {
        return ResponseCookie.from(Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path("/")
                .httpOnly(true) // XSS 공격 방지
                .secure(isProduction) // 프로덕션에서만 HTTPS 필수
                .sameSite("Strict") // CSRF 공격 방지
                .maxAge(Constants.Jwt.REFRESH_TOKEN_EXPIRY) // 30일
                .build();
    }

    /**
     * Access Token 쿠키 삭제용 쿠키 생성
     * @param isProduction 프로덕션 환경 여부
     * @return ResponseCookie (maxAge=0으로 설정되어 삭제됨)
     */
    public static ResponseCookie createAccessTokenDeleteCookie(boolean isProduction) {
        return ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, "")
                .path("/")
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
    }

    /**
     * Refresh Token 쿠키 삭제용 쿠키 생성
     * @param isProduction 프로덕션 환경 여부
     * @return ResponseCookie (maxAge=0으로 설정되어 삭제됨)
     */
    public static ResponseCookie createRefreshTokenDeleteCookie(boolean isProduction) {
        return ResponseCookie.from(Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
    }

    /**
     * 환경에서 프로덕션 여부 확인
     * @param environment Spring Environment
     * @return 프로덕션 환경이면 true
     */
    public static boolean isProduction(Environment environment) {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}

