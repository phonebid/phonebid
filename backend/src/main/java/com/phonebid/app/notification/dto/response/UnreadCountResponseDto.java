package com.phonebid.app.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponseDto {
    private long unreadCount;

    public static UnreadCountResponseDto of(long count) {
        return UnreadCountResponseDto.builder()
                .unreadCount(count)
                .build();
    }
}

