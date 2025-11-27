package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * 채팅방 내 메시지를 작성 시각 기준 오름차순으로 조회.
     * UI에서 시간순 정렬된 메시지 목록을 제공하기 위해 사용한다.
     * 
     * @deprecated 역순 페이징 API 사용을 권장, 전체 메시지를 한 번에 조회하므로 성능 이슈가 있을 수 있음.
     *             대신 {@link #findByChatRoomIdOrderByCreatedAtDesc(UUID, Pageable)}를 사용
     */
    @Deprecated
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(UUID chatRoomId);

    /**
     * 채팅방 내 메시지를 작성 시각 기준 내림차순으로 페이징 조회 (최신 메시지부터).
     * 역순 무한스크롤을 위해 사용한다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param pageable 페이징 정보 (페이지 번호, 크기)
     * @return 최신 메시지부터 정렬된 페이징된 메시지 목록
     */
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId, Pageable pageable);

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

    /**
     * 특정 채팅방에서 특정 사용자가 읽지 않은 메시지 수 조회
     * (상대방이 보낸 메시지 중 읽지 않은 메시지)
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.sender.id != :userId " +
           "AND cm.isRead = false")
    long countUnreadMessagesByChatRoomIdAndUserId(@Param("chatRoomId") UUID chatRoomId, 
                                                   @Param("userId") UUID userId);
}


