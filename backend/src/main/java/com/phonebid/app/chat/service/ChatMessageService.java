package com.phonebid.app.chat.service;

import com.phonebid.app.chat.domain.ChatMessage;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.dto.request.ChatMessageSendRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 채팅 메시지 송수신에 관한 비즈니스 로직을 담당하는 클래스
 * 메시지 발신자의 권한을 검증하고, 메시지를 저장한 뒤 DTO로 반환하여
 * WebSocket 브로드캐스트 등 후속 처리가 가능하도록 함
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // username으로 사용자 조회 (username은 unique 제약조건이 있음)
        User sender = userRepository.findByUsername(request.getSenderId())
                .orElseThrow(() -> new CustomException(MemberErrorCode.USER_NOT_FOUND));

        validateParticipant(chatRoom, sender.getId());

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(request.getMessageType())
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return ChatMessageResponse.from(savedMessage);
    }

    private void validateParticipant(ChatRoom chatRoom, UUID userId) {
        boolean isParticipant = chatRoom.getConsumer().getId().equals(userId)
                || chatRoom.getSeller().getUser().getId().equals(userId);

        if (!isParticipant) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}


