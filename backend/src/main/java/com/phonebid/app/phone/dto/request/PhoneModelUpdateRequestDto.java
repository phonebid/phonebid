package com.phonebid.app.phone.dto.request;

import com.phonebid.app.phone.domain.Brand;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 휴대폰 모델 수정 요청 DTO
 * ID는 path variable로 전달되므로 DTO에서 제외
 */
@Getter
@Setter
@NoArgsConstructor
public class PhoneModelUpdateRequestDto {

    private Brand brand;

    private String model;

    private String modelNumber;

    @Positive(message = "출시가는 양수여야 합니다.")
    private Integer releasedPrice;

    private LocalDate releasedAt;

    public PhoneModelUpdateRequestDto(Brand brand, String model, String modelNumber, 
                                    Integer releasedPrice, LocalDate releasedAt) {
        this.brand = brand;
        this.model = model;
        this.modelNumber = modelNumber;
        this.releasedPrice = releasedPrice;
        this.releasedAt = releasedAt;
    }

    /**
     * 부분 업데이트를 위해 null이 아닌 필드만 확인하는 유틸리티 메서드들
     */
    public boolean hasBrand() {
        return brand != null;
    }

    public boolean hasModel() {
        return model != null;
    }

    public boolean hasModelNumber() {
        return modelNumber != null;
    }

    public boolean hasReleasedPrice() {
        return releasedPrice != null;
    }

    public boolean hasReleasedAt() {
        return releasedAt != null;
    }
}
