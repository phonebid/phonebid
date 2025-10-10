package com.phonebid.app.phone.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.util.List;
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

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("옵션 목록")
    private List<PhoneOption> options;

    @Builder
    public PhoneModel(Brand brand, String model, String modelNumber, 
                     Integer releasedPrice, LocalDate releasedAt) {
        this.brand = brand;
        this.model = model;
        this.modelNumber = modelNumber;
        this.releasedPrice = releasedPrice;
        this.releasedAt = releasedAt;
    }


    /**
     * 모든 필드를 한 번에 업데이트 (null이 아닌 값만)
     */
    public void updateAll(Brand brand, String model, String modelNumber, 
                         Integer releasedPrice, LocalDate releasedAt) {
        if (brand != null) {
            this.brand = brand;
        }
        if (model != null && !model.trim().isEmpty()) {
            this.model = model;
        }
        if (modelNumber != null) {
            this.modelNumber = modelNumber;
        }
        if (releasedPrice != null) {
            this.releasedPrice = releasedPrice;
        }
        if (releasedAt != null) {
            this.releasedAt = releasedAt;
        }
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
