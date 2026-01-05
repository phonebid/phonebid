package com.phonebid.app.phone.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PhoneModelImageUploadResponseDto {
    private List<PhoneModelImageResponseDto> images;

    public PhoneModelImageUploadResponseDto(List<PhoneModelImageResponseDto> images) {
        this.images = images;
    }

    public static PhoneModelImageUploadResponseDto from(List<PhoneModelImageResponseDto> images) {
        return new PhoneModelImageUploadResponseDto(images);
    }
}

