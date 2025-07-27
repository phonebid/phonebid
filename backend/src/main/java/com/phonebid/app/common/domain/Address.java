package com.phonebid.app.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Builder
    public Address(String postalCode, String address, String detailAddress) {
        this.postalCode = postalCode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    // 비즈니스 메서드
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            fullAddress.append("(").append(postalCode).append(") ");
        }
        
        if (address != null && !address.trim().isEmpty()) {
            fullAddress.append(address);
        }
        
        if (detailAddress != null && !detailAddress.trim().isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" ");
            }
            fullAddress.append(detailAddress);
        }
        
        return fullAddress.toString().trim();
    }

    public boolean hasDetailAddress() {
        return detailAddress != null && !detailAddress.trim().isEmpty();
    }

    public boolean isComplete() {
        return postalCode != null && !postalCode.trim().isEmpty() &&
               address != null && !address.trim().isEmpty();
    }

    public boolean isEmpty() {
        return (postalCode == null || postalCode.trim().isEmpty()) &&
               (address == null || address.trim().isEmpty()) &&
               (detailAddress == null || detailAddress.trim().isEmpty());
    }
} 