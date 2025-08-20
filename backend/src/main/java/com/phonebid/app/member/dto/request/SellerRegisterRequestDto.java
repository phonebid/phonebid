package com.phonebid.app.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매자 등록 요청 DTO
 * 판매자 신청을 위한 요청 데이터를 담는 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegisterRequestDto {

    @NotBlank(message = "사업자등록번호는 필수입니다")
    @Pattern(regexp = "^\\d{10}$", message = "사업자등록번호는 하이픈(-) 없이 10자리 숫자로 입력해주세요")
    private String businessNumber;

    @NotBlank(message = "매장명은 필수입니다")
    @Size(min = 2, max = 50, message = "매장명은 2자 이상 50자 이하여야 합니다")
    private String storeName;
} 