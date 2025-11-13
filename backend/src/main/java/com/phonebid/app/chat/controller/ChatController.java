package com.phonebid.app.chat.controller;

import com.phonebid.app.chat.dto.request.ChatMessageSendRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.phonebid.app.chat.service.ChatMessageService;
import java.util.HashMap;
import java.util.Map;

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
     * @param chatRoomId
     * @param payload
     */
    @MessageMapping("/chat/{chatRoomId}/typing")
    public void handleTyping(@DestinationVariable String chatRoomId,
                             @Payload Map<String, Object> payload) {
        Map<String, Object> typingEvent = new HashMap<>();
        typingEvent.put("type", "TYPING");
        typingEvent.put("senderId", payload.get("senderId"));
        typingEvent.put("isTyping", payload.get("isTyping"));
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/typing", typingEvent);
    }
}


