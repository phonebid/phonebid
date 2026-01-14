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

import org.hibernate.annotations.Comment;

@Entity
@Table(name = "sellers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seller_id")
    @Comment("판매자 고유 ID (UUID)")
    private UUID sellerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_number", nullable = false)
    @Comment("사업자등록번호")
    private String businessNumber;

    @Column(name = "store_name", nullable = false)
    @Comment("상호명")
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    @Comment("승인 상태 (PENDING, APPROVED, REJECTED)")
    private ApprovalStatus approvalStatus;

    @Embedded
    private Address storeAddress;

    @Column(name = "is_agent", nullable = false)
    @Comment("대리점 여부")
    private Boolean isAgent;

    @Column(name = "representative_name", nullable = false)
    @Comment("대표자명")
    private String representativeName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "postalCode", column = @Column(name = "business_postal_code")),
        @AttributeOverride(name = "address", column = @Column(name = "business_address")),
        @AttributeOverride(name = "detailAddress", column = @Column(name = "business_detail_address"))
    })
    private Address businessAddress;

    @Column(name = "consent_number", nullable = true)
    @Comment("승낙번호")
    private String consentNumber;

    @Column(name = "customer_service_phone", nullable = true)
    @Comment("고객센터 전화번호")
    private String customerServicePhone;

    @Builder
    public Seller(User user, String businessNumber, String storeName, Address storeAddress, 
                  Boolean isAgent, String representativeName, Address businessAddress, 
                  String consentNumber, String customerServicePhone) {
        this.user = user;
        this.businessNumber = businessNumber;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.isAgent = isAgent != null ? isAgent : false;
        this.representativeName = representativeName;
        this.businessAddress = businessAddress;
        this.consentNumber = consentNumber;
        this.customerServicePhone = customerServicePhone;
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