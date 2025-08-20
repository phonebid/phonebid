package com.phonebid.app.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.phonebid.app.common.domain.Address;

/**
 * 판매자 프로필 수정 요청 DTO
 * 판매자 정보 수정을 위한 요청 데이터를 담는 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileUpdateRequestDto {

    // 매장명 (선택적 필드)
    @Size(min = 2, max = 50, message = "매장명은 2자 이상 50자 이하여야 합니다")
    private String storeName;

    // 연락처 (선택적 필드)
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phoneNumber;

    // 이메일 (선택적 필드)
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    // 주소 정보 (선택적 필드)
    private Address storeAddress;
} 