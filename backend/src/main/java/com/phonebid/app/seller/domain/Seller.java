package com.phonebid.app.seller.domain;

import com.phonebid.app.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sellers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "business_number", nullable = false)
    private String businessNumber;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Seller(User user, String businessNumber, String storeName) {
        this.user = user;
        this.userId = user.getId();
        this.businessNumber = businessNumber;
        this.storeName = storeName;
        this.approvalStatus = ApprovalStatus.PENDING; // 기본값: 승인 대기
        this.createdAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public boolean canSell() {
        return approvalStatus.canSell();
    }

    public boolean isPendingApproval() {
        return approvalStatus.isPending();
    }

    public void approve() {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태가 아닌 판매자는 승인할 수 없습니다.");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject() {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태가 아닌 판매자는 거부할 수 없습니다.");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public void updateStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void updateBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }
} 