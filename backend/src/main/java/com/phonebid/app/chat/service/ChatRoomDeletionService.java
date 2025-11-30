package com.phonebid.app.chat.service;

import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.chat.repository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅방 삭제를 처리하는 서비스
 * 각 채팅방 삭제는 독립적인 새 트랜잭션으로 처리되어야 하므로 별도 서비스로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomDeletionService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 단일 채팅방을 삭제하는 트랜잭션 메서드
     * 각 채팅방 삭제는 독립적인 새 트랜잭션으로 처리
     * 
     * @param room 삭제할 채팅방
     * @return 삭제된 메시지 수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteChatRoomInTransaction(ChatRoom room) {
        // 1. 메시지 수 확인 (삭제 전)
        int messageCount = chatMessageRepository.countByChatRoomId(room.getId());
        
        // 2. 메시지 삭제
        chatMessageRepository.deleteByChatRoomId(room.getId());
        
        // 3. UserChatRoom 삭제
        userChatRoomRepository.deleteByChatRoomId(room.getId());
        
        // 4. ChatRoom 삭제
        chatRoomRepository.delete(room);
        
        return messageCount;
    }
}

