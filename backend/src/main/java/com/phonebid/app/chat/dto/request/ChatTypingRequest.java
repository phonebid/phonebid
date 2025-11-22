package com.phonebid.app.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 타이핑 이벤트 요청 DTO
 * 
 * - 기존 Map<String, Object> 타입의 원시 페이로드 사용으로 인한 타입 안전성 부족 문제 해결
 * - 유효성 검증을 통한 데이터 무결성 보장
 */
@Getter
@NoArgsConstructor
public class ChatTypingRequest {

    @NotBlank(message = "발신자 ID는 필수입니다.")
    private String senderId;

    @NotNull(message = "타이핑 상태는 필수입니다.")
    private Boolean isTyping;
}

