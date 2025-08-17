package com.phonebid.app.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.ApprovalStatus;

/**
 * 판매자 프로필 응답 DTO
 * 판매자 정보 조회 응답을 위한 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponseDto {

    private String username;
    private String businessNumber;
    private String storeName;
    private String representativeName;
    private String phoneNumber;
    private String email;
    private String fullAddress;
    private ApprovalStatus approvalStatus;
    private String approvalStatusDisplayName;

    /**
     * Seller 엔티티로부터 SellerProfileResponseDto를 생성하는 팩토리 메서드
     */
    public static SellerProfileResponseDto from(Seller seller) {
        return SellerProfileResponseDto.builder()
                .username(seller.getUser().getUsername())
                .businessNumber(seller.getBusinessNumber())
                .storeName(seller.getStoreName())
                .representativeName(seller.getUser().getName())
                .phoneNumber(seller.getUser().getPhone())
                .email(seller.getUser().getEmail())
                .fullAddress(seller.getStoreAddressSummary())
                .approvalStatus(seller.getApprovalStatus())
                .approvalStatusDisplayName(seller.getApprovalStatus().getDisplayName())
                .build();
    }
} 