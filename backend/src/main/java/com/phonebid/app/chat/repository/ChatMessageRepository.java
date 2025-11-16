package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * 채팅방 내 메시지를 작성 시각 기준 오름차순으로 조회.
     * UI에서 시간순 정렬된 메시지 목록을 제공하기 위해 사용한다.
     */
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(UUID chatRoomId);

    /**
     * 특정 채팅방의 지정된 메시지들만 조회.
     * 읽음 처리 등 메시지 일부에 대한 후속 작업을 할 때 활용한다.
     */
    List<ChatMessage> findByChatRoomIdAndIdIn(UUID chatRoomId, List<UUID> messageIds);

    /**
     * 채팅방의 마지막 메시지 조회 (작성 시각 기준 내림차순 첫 번째)
     */
    Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId);

    /**
     * 특정 채팅방의 메시지 수 조회
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId")
    int countByChatRoomId(@Param("chatRoomId") UUID chatRoomId);

    /**
     * 채팅방의 모든 메시지 삭제 (hard delete), 배치 처리용
     */
    @Modifying
    @Query("DELETE FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") UUID chatRoomId);
}


