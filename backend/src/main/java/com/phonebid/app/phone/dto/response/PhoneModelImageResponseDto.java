package com.phonebid.app.phone.dto.response;

import com.phonebid.app.phone.domain.PhoneModelImage;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class PhoneModelImageResponseDto {
    private UUID id;
    private String imageUrl;
    private Integer displayOrder;

    public PhoneModelImageResponseDto(UUID id, String imageUrl, Integer displayOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public static PhoneModelImageResponseDto from(PhoneModelImage phoneModelImage) {
        return new PhoneModelImageResponseDto(
            phoneModelImage.getId(),
            phoneModelImage.getImageUrl(),
            phoneModelImage.getDisplayOrder()
        );
    }
}

