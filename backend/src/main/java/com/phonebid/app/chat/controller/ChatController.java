package com.phonebid.app.chat.controller;

import com.phonebid.app.chat.dto.request.ChatMessageSendRequest;
import com.phonebid.app.chat.dto.request.ChatTypingRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatTypingEvent;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.service.ChatMessageService;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.security.UserDetailsImpl;
import com.phonebid.app.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final UserDetailsServiceImpl userDetailsService;

    @MessageMapping("/chat/{chatRoomId}/send")
    public void sendMessage(@DestinationVariable String chatRoomId,
                            @Payload ChatMessageSendRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        UserDetailsImpl userDetails = getAuthenticatedUser(headerAccessor);
        
        String authenticatedUsername = userDetails.getUsername();
        if (!authenticatedUsername.equals(request.getSenderId())) {
            log.warn("발신자 불일치: authenticated={}, request.senderId={}", authenticatedUsername, request.getSenderId());
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

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
     * - 인증된 사용자 검증 추가
     * 
     * @param chatRoomId 채팅방 ID
     * @param request 타이핑 이벤트 요청 DTO
     * @param principal 인증된 사용자 Principal
     */
    @MessageMapping("/chat/{chatRoomId}/typing")
    public void handleTyping(@DestinationVariable String chatRoomId,
                             @Payload ChatTypingRequest request,
                             SimpMessageHeaderAccessor headerAccessor) {
        UserDetailsImpl userDetails = getAuthenticatedUser(headerAccessor);
        
        String authenticatedUsername = userDetails.getUsername();
        if (!authenticatedUsername.equals(request.getSenderId())) {
            log.warn("발신자 불일치: authenticated={}, request.senderId={}", authenticatedUsername, request.getSenderId());
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        ChatTypingEvent typingEvent = new ChatTypingEvent(
                request.getSenderId(),
                request.getIsTyping()
        );
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/typing", typingEvent);
    }

    /**
     * SimpMessageHeaderAccessor에서 인증된 사용자 정보를 가져옵니다.
     * sessionAttributes에서 username을 가져와서 UserDetails를 로드합니다.
     */
    private UserDetailsImpl getAuthenticatedUser(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            log.error("SimpMessageHeaderAccessor가 null입니다.");
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // 먼저 Principal에서 시도
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            if (principal instanceof UserDetailsImpl) {
                return (UserDetailsImpl) principal;
            }
            if (principal instanceof Authentication) {
                Authentication authentication = (Authentication) principal;
                Object authPrincipal = authentication.getPrincipal();
                if (authPrincipal instanceof UserDetailsImpl) {
                    return (UserDetailsImpl) authPrincipal;
                }
            }
        }

        // Principal이 없으면 sessionAttributes에서 username 가져오기
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            if (username != null) {
                try {
                    return (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                } catch (Exception e) {
                    log.error("사용자 정보 로드 실패: username={}", username, e);
                    throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }
            }
        }

        log.error("인증된 사용자 정보를 찾을 수 없습니다. Principal={}, sessionAttributes={}", 
                principal, sessionAttributes);
        throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }
}


