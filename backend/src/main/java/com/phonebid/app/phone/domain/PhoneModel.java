package com.phonebid.app.phone.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "phone_models", 
       uniqueConstraints = @UniqueConstraint(name = "uq_phone_models_brand_model", 
                                           columnNames = {"brand", "model"}),
       indexes = @Index(name = "idx_phone_models_brand", columnList = "brand"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @Comment("모델 고유 ID (UUID)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "brand", nullable = false)
    @Comment("브랜드 (APPLE, SAMSUNG, LG 등)")
    private Brand brand;

    @Column(name = "model", nullable = false)
    @Comment("모델명 예: \"iPhone 16\", \"Galaxy S24\"")
    private String model;

    @Column(name = "model_number")
    @Comment("제조사 모델 번호 예: \"A3101\"(선택)")
    private String modelNumber;

    @Column(name = "released_price")
    @Comment("출시가(원)")
    private Integer releasedPrice;

    @Column(name = "released_at")
    @Comment("출시일")
    private LocalDate releasedAt;

    @Builder
    public PhoneModel(Brand brand, String model, String modelNumber, 
                     Integer releasedPrice, LocalDate releasedAt) {
        this.brand = brand;
        this.model = model;
        this.modelNumber = modelNumber;
        this.releasedPrice = releasedPrice;
        this.releasedAt = releasedAt;
    }

    // 비즈니스 메서드
    public void updateModel(String model) {
        this.model = model;
    }

    public void updateModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public void updateReleasedPrice(Integer releasedPrice) {
        this.releasedPrice = releasedPrice;
    }

    public void updateReleasedAt(LocalDate releasedAt) {
        this.releasedAt = releasedAt;
    }

    public String getFullModelName() {
        return brand.getDisplayName() + " " + model;
    }

    public boolean hasModelNumber() {
        return modelNumber != null && !modelNumber.trim().isEmpty();
    }

    public boolean hasReleasedPrice() {
        return releasedPrice != null && releasedPrice > 0;
    }

    public boolean isReleased() {
        return releasedAt != null && !releasedAt.isAfter(LocalDate.now());
    }

    public String getModelSummary() {
        StringBuilder summary = new StringBuilder(getFullModelName());
        if (hasModelNumber()) {
            summary.append(" (").append(modelNumber).append(")");
        }
        if (hasReleasedPrice()) {
            summary.append(" - ").append(String.format("%,d원", releasedPrice));
        }
        return summary.toString();
    }
}
