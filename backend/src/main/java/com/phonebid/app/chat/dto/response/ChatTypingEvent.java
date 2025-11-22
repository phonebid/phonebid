package com.phonebid.app.chat.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 타이핑 이벤트 응답 DTO
 * 
 * - 기존 Map<String, Object> 타입의 원시 이벤트 객체를 타입 안전한 DTO로 변경
 * - 클라이언트와의 계약을 명확하게 정의하여 유지보수성 향상
 */
@Getter
@NoArgsConstructor
public class ChatTypingEvent {

    private String type = "TYPING";
    private String senderId;
    private Boolean isTyping;

    public ChatTypingEvent(String senderId, Boolean isTyping) {
        this.senderId = senderId;
        this.isTyping = isTyping;
    }
}

