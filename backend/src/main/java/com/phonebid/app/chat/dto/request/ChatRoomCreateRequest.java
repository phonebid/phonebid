package com.phonebid.app.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomCreateRequest {

    @NotNull(message = "견적 ID는 필수입니다.")
    private UUID quoteId;

    @NotNull(message = "구매자 ID는 필수입니다.")
    private UUID consumerId;

    @NotNull(message = "판매자 ID는 필수입니다.")
    private UUID sellerId;
}


