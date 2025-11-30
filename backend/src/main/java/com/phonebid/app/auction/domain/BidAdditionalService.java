package com.phonebid.app.auction.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "bid_additional_services", indexes = {
    @Index(name = "idx_bid_additional_services_bid_id", columnList = "bid_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidAdditionalService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("부가서비스 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @Column(name = "service_name", nullable = false, length = 100)
    @Comment("부가서비스 이름")
    private String serviceName;

    @Column(name = "service_price", nullable = false)
    @Comment("부가서비스 월 요금 (원)")
    private Integer servicePrice;

    @Column(name = "description", length = 500)
    @Comment("부가서비스 설명")
    private String description;

    @Column(name = "mandatory")
    @Comment("필수 가입 여부")
    private Boolean mandatory;

    @Column(name = "cancellable_after_months")
    @Comment("해지 가능 개월 수 (예: 3개월 후 해지 가능)")
    private Integer cancellableAfterMonths;

    @Builder
    public BidAdditionalService(Bid bid, String serviceName, Integer servicePrice,
                                 String description, Boolean mandatory, Integer cancellableAfterMonths) {
        this.bid = bid;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.description = description;
        this.mandatory = mandatory != null ? mandatory : false;
        this.cancellableAfterMonths = cancellableAfterMonths;
    }

    public boolean isMandatory() {
        return mandatory != null && mandatory;
    }

    public boolean isCancellable() {
        return cancellableAfterMonths != null && cancellableAfterMonths > 0;
    }

    public String getCancellableDescription() {
        if (!isCancellable()) {
            return null;
        }
        return String.format("%d개월 후 %s 해지가 가능합니다.", cancellableAfterMonths, serviceName);
    }
}

