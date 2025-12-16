package com.phonebid.app.mypage.dto.response;

import com.phonebid.app.trade.domain.Contract;
import com.phonebid.app.trade.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PurchaseHistoryResponseDto {

    private UUID contractId;
    private String productName;
    private LocalDateTime transactionDate;
    private String productImageUrl;
    private ProductInfoDto productInfo;
    private Integer price;
    private String status;
    private Boolean canReview;

    public static PurchaseHistoryResponseDto from(Contract contract, Payment payment) {
        PurchaseHistoryResponseDto dto = new PurchaseHistoryResponseDto();
        
        dto.contractId = contract.getId();
        dto.productName = contract.getQuote().getPhoneModel().getFullModelName();
        dto.transactionDate = contract.getSignedAt() != null 
            ? contract.getSignedAt() 
            : (payment != null && payment.getPaidAt() != null ? payment.getPaidAt() : contract.getCreatedAt());
        dto.productImageUrl = null; // 추후 이미지 URL 추가 예정
        dto.productInfo = ProductInfoDto.from(contract);
        dto.price = contract.getContractAmount();
        dto.status = determineStatus(contract, payment);
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
        // 구매완료 상태이고 결제가 완료된 경우에만 후기 작성 가능
        // TODO: 리뷰 엔티티 확인 후 실제 리뷰 존재 여부 체크
        return contract.getStatus().isSigned() 
            && payment != null 
            && payment.getStatus().isPaid();
    }

    @Getter
    @NoArgsConstructor
    public static class ProductInfoDto {
        private String brand;
        private String model;
        private String storage;
        private String color;
        private String carrier;

        public static ProductInfoDto from(Contract contract) {
            ProductInfoDto dto = new ProductInfoDto();
            var phoneModel = contract.getQuote().getPhoneModel();
            var quote = contract.getQuote();
            
            dto.brand = phoneModel.getBrand().getDisplayName();
            dto.model = phoneModel.getModel();
            dto.storage = quote.getStorage() != null ? quote.getStorage().getDisplayLabel() : null;
            dto.color = quote.getColor() != null ? quote.getColor().getDisplayLabel() : null;
            dto.carrier = quote.getCarrier().getDisplayName();
            
            return dto;
        }
    }
}

