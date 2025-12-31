package com.phonebid.app.mypage.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.TradeErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_delivery_addresses", indexes = {
    @Index(name = "idx_user_delivery_addresses_user_id", columnList = "user_id"),
    @Index(name = "idx_user_delivery_addresses_is_default", columnList = "is_default")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDeliveryAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("배송지 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("배송지 소유자")
    private User user;

    @Column(name = "address_name", nullable = false)
    @Comment("배송지명")
    private String addressName;

    @Column(name = "recipient_name", nullable = false)
    @Comment("수령자 이름")
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    @Comment("수령자 전화번호")
    private String recipientPhone;

    @Column(name = "postal_code", nullable = false)
    @Comment("우편번호")
    private String postalCode;

    @Column(name = "address", nullable = false)
    @Comment("주소")
    private String address;

    @Column(name = "detail_address")
    @Comment("상세주소")
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    @Comment("기본 배송지 여부")
    private Boolean isDefault;

    @Builder
    public UserDeliveryAddress(User user, String addressName, String recipientName, 
                               String recipientPhone, String postalCode, String address, 
                               String detailAddress, Boolean isDefault) {
        validateDeliveryAddress(addressName, recipientName, recipientPhone, postalCode, address);
        
        this.user = user;
        this.addressName = addressName.trim();
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.postalCode = postalCode;
        this.address = address;
        this.detailAddress = detailAddress != null ? detailAddress.trim() : null;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    public void updateAddressName(String addressName) {
        validateAddressName(addressName);
        this.addressName = addressName.trim();
    }

    public void updateRecipientName(String recipientName) {
        validateRecipientName(recipientName);
        this.recipientName = recipientName;
    }

    public void updateRecipientPhone(String recipientPhone) {
        validateRecipientPhone(recipientPhone);
        this.recipientPhone = recipientPhone;
    }

    public void updatePostalCode(String postalCode) {
        validatePostalCode(postalCode);
        this.postalCode = postalCode;
    }

    public void updateAddress(String address) {
        validateAddress(address);
        this.address = address;
    }

    public void updateDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress != null ? detailAddress.trim() : null;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault != null ? isDefault : false;
    }

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

    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDelete);
    }

    private void validateDeliveryAddress(String addressName, String recipientName, String recipientPhone, 
                                       String postalCode, String address) {
        validateAddressName(addressName);
        validateRecipientName(recipientName);
        validateRecipientPhone(recipientPhone);
        validatePostalCode(postalCode);
        validateAddress(address);
    }

    private void validateAddressName(String addressName) {
        if (addressName == null || addressName.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_ADDRESS_NAME);
        }
    }

    private void validateRecipientName(String recipientName) {
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_RECIPIENT_NAME);
        }
    }

    private void validateRecipientPhone(String recipientPhone) {
        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_RECIPIENT_PHONE);
        }
        
        if (!recipientPhone.matches("^[0-9-+()\\s]+$")) {
            throw new CustomException(TradeErrorCode.INVALID_PHONE_FORMAT);
        }
    }

    private void validatePostalCode(String postalCode) {
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_POSTAL_CODE);
        }
        
        if (!postalCode.matches("^\\d{5}$")) {
            throw new CustomException(TradeErrorCode.INVALID_POSTAL_CODE_FORMAT);
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_ADDRESS);
        }
    }
}

