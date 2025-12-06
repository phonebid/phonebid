package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class BidResponseDto {

    private UUID id;
    private UUID quoteId;
    
    // 판매자 정보
    private UUID sellerId;
    private String sellerStoreName;
    private Double sellerRating;
    
    // 가격 정보
    private Integer price;
    private Integer installmentPrincipal;
    private Integer additionalSubsidy;
    private Integer totalMaintenanceCost;
    
    // 요금제 정보
    private String pricePlanName;
    private Integer pricePlanPrice;
    
    // 부가서비스 정보
    private List<AdditionalServiceResponseDto> additionalServices;
    private Integer additionalServicesCount;
    private Integer additionalServicesTotalPrice;
    
    // 개통 정보
    private PurchaseMethod purchaseMethod;
    private Carrier carrier;
    private Carrier currentCarrier;
    private ActivationMethod activationMethod;
    private Integer contractMonths;
    
    // 배송 정보
    private Integer deliveryDays;
    
    // 상태 정보
    private BidStatus status;
    private LocalDateTime createdAt;

    public static BidResponseDto from(Bid bid) {
        List<AdditionalServiceResponseDto> additionalServiceDtos = bid.getAdditionalServiceList().stream()
                .map(AdditionalServiceResponseDto::from)
                .collect(Collectors.toList());
        
        Integer additionalServicesTotalPrice = bid.getAdditionalServiceList().stream()
                .mapToInt(service -> service.getServicePrice() != null ? service.getServicePrice() : 0)
                .sum();
        
        Integer totalMaintenanceCost = calculateTotalMaintenanceCost(bid, additionalServicesTotalPrice);
        
        return BidResponseDto.builder()
                .id(bid.getId())
                .quoteId(bid.getQuote().getId())
                .sellerId(bid.getSeller().getSellerId())
                .sellerStoreName(bid.getSeller().getStoreName())
                .sellerRating(bid.getRatingSnapshot())
                .price(bid.getPrice())
                .installmentPrincipal(bid.getInstallmentPrincipal())
                .additionalSubsidy(bid.getAdditionalSubsidy())
                .totalMaintenanceCost(totalMaintenanceCost)
                .pricePlanName(bid.getPricePlan() != null ? bid.getPricePlan().getPlanName() : null)
                .pricePlanPrice(bid.getPricePlan() != null ? bid.getPricePlan().getPlanPrice() : null)
                .additionalServices(additionalServiceDtos)
                .additionalServicesCount(additionalServiceDtos.size())
                .additionalServicesTotalPrice(additionalServicesTotalPrice)
                .purchaseMethod(bid.getPurchaseMethod())
                .carrier(bid.getCarrier())
                .currentCarrier(bid.getCurrentCarrier())
                .activationMethod(bid.getActivationMethod())
                .contractMonths(bid.getContractMonths())
                .deliveryDays(bid.getDeliveryDays())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    private static Integer calculateTotalMaintenanceCost(Bid bid, Integer additionalServicesTotalPrice) {
        Integer months = bid.getContractMonths() != null ? bid.getContractMonths() : 24;
        Integer planPrice = bid.getPricePlan() != null ? bid.getPricePlan().getPlanPrice() : 0;
        
        return (planPrice + additionalServicesTotalPrice) * months;
    }
}

