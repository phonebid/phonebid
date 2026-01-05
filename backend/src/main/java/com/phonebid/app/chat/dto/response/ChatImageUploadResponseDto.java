package com.phonebid.app.chat.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatImageUploadResponseDto {
    private String imageUrl;

    public ChatImageUploadResponseDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static ChatImageUploadResponseDto from(String imageUrl) {
        return new ChatImageUploadResponseDto(imageUrl);
    }
}

