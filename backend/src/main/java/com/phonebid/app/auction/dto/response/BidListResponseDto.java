package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.BidStatus;
import com.phonebid.app.auction.domain.PricePlan;
import com.phonebid.app.auction.domain.PricePlanCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class BidListResponseDto {

    private UUID id;
    private UUID sellerId;
    private String sellerStoreName;
    private Double sellerRating;
    private Integer installmentPrincipal;
    private Integer totalMaintenanceCost;
    private UUID pricePlanId;
    private String pricePlanName;
    private Integer pricePlanPrice;
    private PricePlanCategory pricePlanCategory;
    private BidStatus status;
    private LocalDateTime createdAt;

    public static BidListResponseDto from(Bid bid) {
        Integer totalMaintenanceCost = calculateTotalMaintenanceCost(bid);
        PricePlan pricePlan = bid.getPricePlan();
        
        return BidListResponseDto.builder()
                .id(bid.getId())
                .sellerId(bid.getSeller().getSellerId())
                .sellerStoreName(bid.getSeller().getStoreName())
                .sellerRating(bid.getRatingSnapshot())
                .installmentPrincipal(bid.getInstallmentPrincipal())
                .totalMaintenanceCost(totalMaintenanceCost)
                .pricePlanId(pricePlan != null ? pricePlan.getId() : null)
                .pricePlanName(pricePlan != null ? pricePlan.getPlanName() : null)
                .pricePlanPrice(pricePlan != null ? pricePlan.getMonthlyFee() : null)
                .pricePlanCategory(pricePlan != null ? pricePlan.getCategory() : null)
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    private static Integer calculateTotalMaintenanceCost(Bid bid) {
        Integer months = bid.getContractMonths() != null ? bid.getContractMonths() : 24;
        Integer planPrice = bid.getPricePlan() != null ? bid.getPricePlan().getMonthlyFee() : 0;
        Integer additionalServiceTotal = bid.getAdditionalServiceList().stream()
                .mapToInt(service -> service.getServicePrice() != null ? service.getServicePrice() : 0)
                .sum();
        
        return (planPrice + additionalServiceTotal) * months;
    }
}

