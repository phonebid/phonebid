package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatRoom;
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
}


