package com.phonebid.app.chat.dto.request;

import com.phonebid.app.chat.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageSendRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private UUID chatRoomId;

    @NotBlank(message = "발신자 ID는 필수입니다.")
    private String senderId; // username (unique 제약조건)

    @NotNull(message = "메시지 타입은 필수입니다.")
    private MessageType messageType;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
}


