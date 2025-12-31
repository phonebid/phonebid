package com.phonebid.app.mypage.dto.response;

import com.phonebid.app.mypage.domain.UserDeliveryAddress;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeliveryAddressResponseDto {

    private UUID addressId;
    private String addressName;
    private String recipientName;
    private String phone;
    private String postalCode;
    private String address;
    private String detailAddress;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    public static DeliveryAddressResponseDto from(UserDeliveryAddress userDeliveryAddress) {
        DeliveryAddressResponseDto dto = new DeliveryAddressResponseDto();
        dto.addressId = userDeliveryAddress.getId();
        dto.addressName = userDeliveryAddress.getAddressName();
        dto.recipientName = userDeliveryAddress.getRecipientName();
        dto.phone = userDeliveryAddress.getRecipientPhone();
        dto.postalCode = userDeliveryAddress.getPostalCode();
        dto.address = userDeliveryAddress.getAddress();
        dto.detailAddress = userDeliveryAddress.getDetailAddress();
        dto.isDefault = userDeliveryAddress.getIsDefault();
        dto.createdAt = userDeliveryAddress.getCreatedAt();
        return dto;
    }
}

