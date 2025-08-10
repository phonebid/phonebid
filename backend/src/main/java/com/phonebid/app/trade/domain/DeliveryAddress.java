package com.phonebid.app.trade.domain;

import com.phonebid.app.common.errorcode.TradeErrorCode;
import com.phonebid.app.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAddress {

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Builder
    public DeliveryAddress(String recipientName, String recipientPhone, 
                          String postalCode, String address, String detailAddress) {
        validateDeliveryAddress(recipientName, recipientPhone, postalCode, address);
        
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.postalCode = postalCode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    // 비즈니스 메서드
    public String getFullAddress() {
        if (detailAddress != null && !detailAddress.trim().isEmpty()) {
            return String.format("(%s) %s %s", postalCode, address, detailAddress);
        }
        return String.format("(%s) %s", postalCode, address);
    }

    public String getRecipientInfo() {
        return String.format("%s (%s)", recipientName, recipientPhone);
    }

    public boolean hasDetailAddress() {
        return detailAddress != null && !detailAddress.trim().isEmpty();
    }

    // 검증 메서드
    private void validateDeliveryAddress(String recipientName, String recipientPhone, 
                                       String postalCode, String address) {
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_RECIPIENT_NAME);
        }
        
        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_RECIPIENT_PHONE);
        }
        
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_POSTAL_CODE);
        }
        
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_ADDRESS);
        }
        
        // 전화번호 형식 간단 검증
        if (!recipientPhone.matches("^[0-9-+()\\s]+$")) {
            throw new CustomException(TradeErrorCode.INVALID_PHONE_FORMAT);
        }
        
        // 우편번호 형식 검증 (5자리 숫자)
        if (!postalCode.matches("^\\d{5}$")) {
            throw new CustomException(TradeErrorCode.INVALID_POSTAL_CODE_FORMAT);
        }
    }
} 