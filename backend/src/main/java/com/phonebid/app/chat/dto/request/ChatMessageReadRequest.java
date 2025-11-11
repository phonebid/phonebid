package com.phonebid.app.chat.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageReadRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private UUID chatRoomId;

    @NotEmpty(message = "읽음 처리할 메시지 ID 목록은 비어 있을 수 없습니다.")
    private List<UUID> messageIds;
}


