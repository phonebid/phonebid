package com.phonebid.app.phone.dto.request;

import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption.OptionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 휴대폰 모델 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PhoneModelCreateRequestDto {

    @NotNull(message = "브랜드는 필수입니다.")
    private Brand brand;

    @NotBlank(message = "모델명은 필수입니다.")
    private String model;

    private String modelNumber;

    @Positive(message = "출시가는 양수여야 합니다.")
    private Integer releasedPrice;

    private LocalDate releasedAt;

    // 옵션 동시 생성용 리스트 (선택)
    private List<OptionItem> options;

    
    public PhoneModelCreateRequestDto(Brand brand, String model, String modelNumber, 
                                    Integer releasedPrice, LocalDate releasedAt, List<OptionItem> options) {
        this.brand = brand;
        this.model = model;
        this.modelNumber = modelNumber;
        this.releasedPrice = releasedPrice;
        this.releasedAt = releasedAt;
        this.options = options;
    }

    public PhoneModel toEntity() {
        return PhoneModel.builder()
            .brand(brand)
            .model(model)
            .modelNumber(modelNumber)
            .releasedPrice(releasedPrice)
            .releasedAt(releasedAt)
            .build();
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OptionItem {
        @NotNull(message = "옵션 타입은 필수입니다.")
        private OptionType type; // COLOR, STORAGE

        @NotBlank(message = "옵션 값은 필수입니다.")
        private String value; // 예: Black, 128

        private String displayLabel; // 예: 블랙, 128GB

        public OptionItem(OptionType type, String value, String displayLabel) {
            this.type = type;
            this.value = value;
            this.displayLabel = displayLabel;
        }
    }
}
