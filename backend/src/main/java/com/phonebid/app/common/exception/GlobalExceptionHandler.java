package com.phonebid.app.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * 애플리케이션 전체에서 발생하는 예외를 처리하는 클래스
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * CustomException 처리
     * @param e CustomException
     * @return 에러 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {} - {}", e.getErrorCode().getMessage(), e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getErrorCode().getClass().getSimpleName());
        errorResponse.put("message", e.getMessage());
        errorResponse.put("status", e.getErrorCode().getStatus().value());
        
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
    }
    
    /**
     * 일반적인 예외 처리
     * @param e Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "INTERNAL_SERVER_ERROR");
        errorResponse.put("message", "서버 내부 오류가 발생했습니다");
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }
} 