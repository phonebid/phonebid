package com.phonebid.app.phone.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "phone_model_images", indexes = {
    @Index(name = "idx_phone_model_images_model_id", columnList = "model_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneModelImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("핸드폰 모델 이미지 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    @Comment("핸드폰 모델 ID")
    private PhoneModel phoneModel;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    @Comment("이미지 URL")
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    @Comment("이미지 표시 순서")
    private Integer displayOrder;

    @Builder
    public PhoneModelImage(PhoneModel phoneModel, String imageUrl, Integer displayOrder) {
        this.phoneModel = phoneModel;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}

