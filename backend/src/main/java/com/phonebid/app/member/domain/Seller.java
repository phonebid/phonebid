package com.phonebid.app.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.phonebid.app.common.domain.Address;
import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;

@Entity
@Table(name = "sellers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seller_id")
    private UUID sellerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_number", nullable = false)
    private String businessNumber;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @Embedded
    private Address storeAddress;

    @Builder
    public Seller(User user, String businessNumber, String storeName, Address storeAddress) {
        this.user = user;
        this.businessNumber = businessNumber;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.approvalStatus = ApprovalStatus.PENDING; // 기본값: 승인 대기
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
            throw new CustomException(MemberErrorCode.SELLER_CANNOT_APPROVE);
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject() {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new CustomException(MemberErrorCode.SELLER_CANNOT_REJECT);
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public void updateStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void updateBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public void updateStoreAddress(Address storeAddress) {
        this.storeAddress = storeAddress;
    }

    public boolean hasStoreAddress() {
        return storeAddress != null && !storeAddress.isEmpty();
    }

    public String getStoreAddressSummary() {
        if (!hasStoreAddress()) {
            return "주소 정보 없음";
        }
        return storeAddress.getFullAddress();
    }
} 