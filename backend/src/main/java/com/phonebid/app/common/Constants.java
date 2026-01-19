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
         * 일반 로그인 시 토큰/쿠키 만료 시간: 1시간
         */
        public static final Duration DEFAULT_EXPIRY = Duration.ofHours(1);
        
        /**
         * 일반 로그인 시 토큰 만료 시간 (밀리초): 60분
         */
        public static final long DEFAULT_EXPIRY_MILLIS = 60 * 60 * 1000L; // 60분
        
        /**
         * 로그인 상태 유지 시 토큰 만료 시간 (밀리초): 30일
         */
        public static final long KEEP_LOGGED_IN_EXPIRY_MILLIS = 30L * 24 * 60 * 60 * 1000L; // 30일
    }
}

