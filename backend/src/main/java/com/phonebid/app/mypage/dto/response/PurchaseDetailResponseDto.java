package com.phonebid.app.mypage.dto.response;

import com.phonebid.app.trade.domain.Contract;
import com.phonebid.app.trade.domain.Delivery;
import com.phonebid.app.trade.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PurchaseDetailResponseDto {

    private UUID contractId;
    private String productName;
    private LocalDateTime transactionDate;
    private String productImageUrl;
    private PurchaseHistoryResponseDto.ProductInfoDto productInfo;
    private Integer price;
    private String status;
    private SellerInfoDto sellerInfo;
    private PaymentInfoDto paymentInfo;
    private DeliveryInfoDto deliveryInfo;
    private Boolean canReview;

    public static PurchaseDetailResponseDto from(Contract contract, Payment payment, Delivery delivery) {
        PurchaseDetailResponseDto dto = new PurchaseDetailResponseDto();
        
        dto.contractId = contract.getId();
        dto.productName = contract.getQuote().getPhoneModel().getFullModelName();
        dto.transactionDate = contract.getSignedAt() != null 
            ? contract.getSignedAt() 
            : (payment != null && payment.getPaidAt() != null ? payment.getPaidAt() : contract.getCreatedAt());
        dto.productImageUrl = null; // 추후 이미지 URL 추가 예정
        dto.productInfo = PurchaseHistoryResponseDto.ProductInfoDto.from(contract);
        dto.price = contract.getContractAmount();
        dto.status = determineStatus(contract, payment);
        dto.sellerInfo = SellerInfoDto.from(contract);
        dto.paymentInfo = payment != null ? PaymentInfoDto.from(payment) : null;
        dto.deliveryInfo = delivery != null ? DeliveryInfoDto.from(delivery) : null;
        dto.canReview = canReview(contract, payment);
        
        return dto;
    }

    private static String determineStatus(Contract contract, Payment payment) {
        if (contract.getStatus().isCancelled()) {
            return "취소/환불";
        }
        if (payment != null && payment.getStatus().isPaid()) {
            return "거래완료";
        }
        return "진행중";
    }

    private static Boolean canReview(Contract contract, Payment payment) {
        return contract.getStatus().isSigned() 
            && payment != null 
            && payment.getStatus().isPaid();
    }

    @Getter
    @NoArgsConstructor
    public static class SellerInfoDto {
        private String sellerName;
        private Double rating;

        public static SellerInfoDto from(Contract contract) {
            SellerInfoDto dto = new SellerInfoDto();
            var seller = contract.getSelectedBid().getSeller();
            
            dto.sellerName = seller.getStoreName();
            // TODO: Seller 엔티티에 rating 필드 추가 후 사용
            dto.rating = contract.getSelectedBid().getRatingSnapshot();
            
            return dto;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentInfoDto {
        private String method;
        private LocalDateTime paidAt;

        public static PaymentInfoDto from(Payment payment) {
            PaymentInfoDto dto = new PaymentInfoDto();
            dto.method = payment.getMethod().getDisplayName();
            dto.paidAt = payment.getPaidAt();
            return dto;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DeliveryInfoDto {
        private String status;
        private String trackingNumber;

        public static DeliveryInfoDto from(Delivery delivery) {
            DeliveryInfoDto dto = new DeliveryInfoDto();
            dto.status = delivery.getStatus().getDisplayName();
            dto.trackingNumber = delivery.getInvoiceNumber();
            return dto;
        }
    }
}

