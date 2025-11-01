package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatRoom;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    /**
     * 동일 견적·구매자·판매자 조합으로 이미 열린 채팅방이 있는지 확인.
     * 중복 채팅방 생성을 방지하기 위해 사용한다.
     */
    Optional<ChatRoom> findByQuoteIdAndConsumerIdAndSellerSellerId(UUID quoteId, UUID consumerId, UUID sellerId);
}


