package com.phonebid.app.chat.service;

import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.chat.repository.UserChatRoomRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅방 관련 배치 작업을 처리하는 서비스
 * 양쪽 사용자 모두 삭제한 채팅방을 주기적으로 정리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomBatchService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 양쪽 사용자 모두 삭제한 채팅방을 정리하는 배치 작업
     * 매일 새벽 3시에 실행
     */
    @Scheduled(cron = "0 0 3 ? * SUN") // 매주 일요일 새벽 3시에 실행
    public void cleanupDeletedChatRooms() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("=== 채팅방 정리 배치 작업 시작 ===");

        try {
            // DB 레벨에서 양쪽 모두 삭제한 채팅방만 조회
            List<ChatRoom> fullyDeletedRooms = chatRoomRepository.findFullyDeletedChatRooms();

            if (fullyDeletedRooms.isEmpty()) {
                log.info("정리할 채팅방이 없습니다.");
                return;
            }

            log.info("정리 대상 채팅방 수: {}", fullyDeletedRooms.size());

            int deletedChatRoomCount = 0;
            int deletedMessageCount = 0;
            int failedCount = 0;

            // 각 채팅방별로 별도 트랜잭션으로 삭제
            for (ChatRoom room : fullyDeletedRooms) {
                try {
                    int messageCount = deleteChatRoomInTransaction(room);
                    deletedChatRoomCount++;
                    deletedMessageCount += messageCount;
                    
                    log.debug("채팅방 삭제 완료: chatRoomId={}, 메시지 수={}", 
                        room.getId(), messageCount);
                } catch (Exception e) {
                    failedCount++;
                    log.error("채팅방 삭제 실패: chatRoomId={}", room.getId(), e);
                }
            }

            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            log.info("=== 채팅방 정리 배치 작업 완료 ===");
            log.info("실행 시간: {}초", duration.getSeconds());
            log.info("성공: {}개, 실패: {}개", deletedChatRoomCount, failedCount);
            log.info("삭제된 메시지: {}개", deletedMessageCount);

        } catch (Exception e) {
            log.error("채팅방 정리 배치 작업 중 예외 발생", e);
            throw e;
        }
    }

    /**
     * 단일 채팅방을 삭제하는 트랜잭션 메서드
     * 각 채팅방 삭제는 독립적인 트랜잭션으로 처리
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

