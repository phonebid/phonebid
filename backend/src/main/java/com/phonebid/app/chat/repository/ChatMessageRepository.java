package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
}


