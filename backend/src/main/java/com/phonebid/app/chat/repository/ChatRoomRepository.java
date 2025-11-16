package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    /**
     * 견적 ID로 채팅방 조회.
     * 한 견적당 하나의 채팅방만 존재하므로 (quote_id unique 제약) quoteId만으로 조회 가능.
     * 중복 채팅방 생성을 방지하기 위해 사용한다.
     */
    Optional<ChatRoom> findByQuoteId(UUID quoteId);

    /**
     * 사용자가 참여한 채팅방 목록을 생성일 기준 내림차순으로 조회 (페이징)
     * 구매자(consumer) 또는 판매자(seller)로 참여한 채팅방을 조회한다.
     */
    @Query("SELECT c FROM ChatRoom c WHERE c.consumer.id = :userId OR c.seller.user.id = :userId ORDER BY c.createdAt DESC")
    Page<ChatRoom> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * 양쪽 사용자 모두 삭제한 채팅방 조회
     * DB 레벨에서 필터링하여 성능 최적화
     * 활성 멤버 수가 0인 채팅방만 조회
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "WHERE (SELECT COUNT(ucr) FROM UserChatRoom ucr " +
           "WHERE ucr.chatRoom.id = cr.id AND ucr.deletedAt IS NULL) = 0")
    List<ChatRoom> findFullyDeletedChatRooms();
}


