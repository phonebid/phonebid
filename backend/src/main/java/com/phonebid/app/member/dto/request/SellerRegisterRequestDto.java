package com.phonebid.app.member.dto.request;

import com.phonebid.app.common.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // 사업자 정보
    @NotBlank(message = "사업자등록번호는 필수입니다")
    @Pattern(regexp = "^\\d{10}$", message = "사업자등록번호는 하이픈(-) 없이 10자리 숫자로 입력해주세요")
    private String businessNumber;

    @NotBlank(message = "사업자등록증 파일은 필수입니다")
    private String businessLicenseFileUrl;

    @NotBlank(message = "상호명은 필수입니다")
    @Size(min = 2, max = 50, message = "상호명은 2자 이상 50자 이하여야 합니다")
    private String storeName;

    @NotBlank(message = "대표자명은 필수입니다")
    @Size(min = 2, max = 50, message = "대표자명은 2자 이상 50자 이하여야 합니다")
    private String representativeName;

    @NotNull(message = "대리점 여부는 필수입니다")
    private Boolean isAgent;

    @Valid
    @NotNull(message = "사업장 주소는 필수입니다")
    private AddressDto businessAddress;

    // 사전승낙서 정보 (대리점이 아닌 경우 필수)
    @Valid
    @NotNull(message = "판매점 주소는 필수입니다")
    private AddressDto storeAddress;

    private String consentNumber;

    private String consentFormFileUrl;

    // 연락처 정보
    @NotBlank(message = "대표 전화번호는 필수입니다")
    @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자와 하이픈(-)만 입력 가능합니다")
    private String representativePhone;

    @NotBlank(message = "이메일 주소는 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Pattern(regexp = "^[0-9-]+$", message = "고객센터 전화번호는 숫자와 하이픈(-)만 입력 가능합니다")
    private String customerServicePhone;

    // 정산 계좌 정보
    @Valid
    @NotNull(message = "정산 계좌 정보는 필수입니다")
    private SettlementAccountDto settlementAccount;

    // 회원 정보
    @Valid
    @NotNull(message = "회원 정보는 필수입니다")
    private SignupRequestDto userInfo;
} 