package com.phonebid.app.common.dto;

import com.phonebid.app.common.domain.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 DTO
 * 주소 정보를 담는 DTO 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String postalCode;

    @NotBlank(message = "주소는 필수입니다")
    @Size(min = 1, max = 200, message = "주소는 1자 이상 200자 이하여야 합니다.")
    private String address;

    @Size(max = 200, message = "상세주소는 200자 이하여야 합니다.")
    private String detailAddress;

    public Address toEntity() {
        return Address.builder()
                .postalCode(postalCode)
                .address(address)
                .detailAddress(detailAddress)
                .build();
    }

    public static AddressDto from(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDto.builder()
                .postalCode(address.getPostalCode())
                .address(address.getAddress())
                .detailAddress(address.getDetailAddress())
                .build();
    }
}

