package com.phonebid.app.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ApiResponse는는 API 응답을 위한 공통적인 포맷을 제공하는 클래스입니다.
 *
 * @param <T> 응답 데이터 타입
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 *  // 주문 성공 응답 예시
 *  CommonResponse<OrderResponse> response = CommonResponse.success(
 *      HttpStatus.OK,
 *      "주문이 성공하였습니다.",
 *      OrderResponse.from(orderService.createOrder(request.toDto(userId)))
 *  );
 * }</pre>
 */
 
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private HttpStatus status;
    private String message;
    private T data;

    /**
     * 성공 응답 생성용 팩토리 메서드
     * @param status HTTP 상태 코드
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
    
    /**
     * 에러 응답 생성용 팩토리 메서드
     * @param status HTTP 상태 코드
     * @param message 에러 메시지
     * @param data 에러 관련 데이터 (선택사항)
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
}