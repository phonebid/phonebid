package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatRoom;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
}


