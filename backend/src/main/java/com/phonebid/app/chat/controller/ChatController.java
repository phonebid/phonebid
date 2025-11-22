package com.phonebid.app.chat.controller;

import com.phonebid.app.chat.dto.request.ChatMessageSendRequest;
import com.phonebid.app.chat.dto.request.ChatTypingRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatTypingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.phonebid.app.chat.service.ChatMessageService;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/{chatRoomId}/send")
    public void sendMessage(@DestinationVariable String chatRoomId,
                            @Payload ChatMessageSendRequest request) {
        ChatMessageResponse response = chatMessageService.sendMessage(request);
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, response);
    }

    /**
     * 타이핑 이벤트 처리 엔드포인트
     * 상대방이 입력 중일 때 타이핑 이벤트를 전송합니다.
     * 
     * 수정 사항:
     * - Map<String, Object> 타입의 원시 페이로드를 ChatTypingRequest DTO로 변경하여 타입 안전성 확보
     * - 응답 이벤트도 ChatTypingEvent DTO로 래핑하여 클라이언트와의 계약 명확화
     * 
     * @param chatRoomId 채팅방 ID
     * @param request 타이핑 이벤트 요청 DTO
     */
    @MessageMapping("/chat/{chatRoomId}/typing")
    public void handleTyping(@DestinationVariable String chatRoomId,
                             @Payload ChatTypingRequest request) {
        ChatTypingEvent typingEvent = new ChatTypingEvent(
                request.getSenderId(),
                request.getIsTyping()
        );
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/typing", typingEvent);
    }
}


