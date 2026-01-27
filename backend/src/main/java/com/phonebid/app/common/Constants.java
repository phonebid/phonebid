package com.phonebid.app.common;

import java.time.Duration;

/**
 * 애플리케이션 전역에서 사용되는 공통 상수 클래스
 */
public final class Constants {
    
    private Constants() {
        // 인스턴스화 방지
    }
    
    /**
     * JWT 토큰 및 쿠키 만료 시간 관련 상수
     */
    public static final class Jwt {
        private Jwt() {
            // 인스턴스화 방지
        }
        
        /**
         * 로그인 상태 유지 시 토큰/쿠키 만료 시간: 30일
         */
        public static final Duration KEEP_LOGGED_IN_EXPIRY = Duration.ofDays(30);
        
        /**
         * 일반 로그인 시 토큰/쿠키 만료 시간: 30분
         */
        public static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(30);
        
        /**
         * 일반 로그인 시 토큰 만료 시간 (밀리초): 30분
         */
        public static final long DEFAULT_EXPIRY_MILLIS = 30 * 60 * 1000L; // 30분
        
        /**
         * 로그인 상태 유지 시 토큰 만료 시간 (밀리초): 30일
         */
        public static final long KEEP_LOGGED_IN_EXPIRY_MILLIS = 30L * 24 * 60 * 60 * 1000L; // 30일
        
        /**
         * Refresh Token 만료 시간: 2주
         */
        public static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(14);
        
        /**
         * Refresh Token 만료 시간 (밀리초): 2주
         */
        public static final long REFRESH_TOKEN_EXPIRY_MILLIS = 14L * 24 * 60 * 60 * 1000L;
        
        /**
         * Refresh Token 쿠키 이름
         */
        public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    }
}

