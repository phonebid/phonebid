package com.phonebid.app.mypage.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileImageUploadResponseDto {
    private String imageUrl;

    public ProfileImageUploadResponseDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static ProfileImageUploadResponseDto from(String imageUrl) {
        return new ProfileImageUploadResponseDto(imageUrl);
    }
}

