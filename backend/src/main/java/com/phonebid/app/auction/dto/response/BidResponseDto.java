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
    
    private UUID sellerId;
    private String sellerStoreName;
    private Double sellerRating;
    
    private Integer price;
    private Integer installmentPrincipal;
    private Integer additionalSubsidy;
    private Integer totalMaintenanceCost;
    
    private UUID pricePlanId;
    private String pricePlanName;
    private Integer pricePlanPrice;
    private PricePlanCategory pricePlanCategory;
    private String pricePlanDataAllowance;
    private String pricePlanThrottleSpeed;
    private String pricePlanVoiceSms;
    
    private List<AdditionalServiceResponseDto> additionalServices;
    private Integer additionalServicesCount;
    private Integer additionalServicesTotalPrice;
    
    private PurchaseMethod purchaseMethod;
    private Carrier carrier;
    private Carrier currentCarrier;
    private ActivationMethod activationMethod;
    private Integer contractMonths;
    
    private Integer deliveryDays;
    
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
        PricePlan pricePlan = bid.getPricePlan();
        
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
                .pricePlanId(pricePlan != null ? pricePlan.getId() : null)
                .pricePlanName(pricePlan != null ? pricePlan.getPlanName() : null)
                .pricePlanPrice(pricePlan != null ? pricePlan.getMonthlyFee() : null)
                .pricePlanCategory(pricePlan != null ? pricePlan.getCategory() : null)
                .pricePlanDataAllowance(pricePlan != null ? pricePlan.getDataAllowanceText() : null)
                .pricePlanThrottleSpeed(pricePlan != null ? pricePlan.getThrottleSpeedText() : null)
                .pricePlanVoiceSms(pricePlan != null ? pricePlan.getVoiceSmsText() : null)
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
        Integer planPrice = bid.getPricePlan() != null ? bid.getPricePlan().getMonthlyFee() : 0;
        
        return (planPrice + additionalServicesTotalPrice) * months;
    }
}

