package com.phonebid.app.common.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 네이버 OAuth2 관련 에러 코드
 * 네이버 로그인 처리 중 발생하는 예외를 정의하는 클래스
 */
@AllArgsConstructor
@Getter
public enum NaverErrorCode implements ErrorCode {
    
    // 네이버 토큰 요청 실패
    NAVER_TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "네이버 토큰 요청에 실패했습니다"),
    
    // 네이버 사용자 정보 요청 실패
    NAVER_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "네이버 사용자 정보 요청에 실패했습니다"),
    
    // 네이버 API 응답 파싱 실패
    NAVER_API_RESPONSE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "네이버 API 응답을 처리할 수 없습니다"),
    
    // 네이버 로그인 처리 실패
    NAVER_LOGIN_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 로그인 처리 중 오류가 발생했습니다"),
    
    // 네이버 설정 누락
    NAVER_CONFIG_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 OAuth2 설정이 누락되었습니다");
    
    private final HttpStatus status;
    private final String message;
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public HttpStatus getStatus() {
        return status;
    }
} 