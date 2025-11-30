package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.BidStatus;
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
    private String pricePlanName;
    private Integer pricePlanPrice;
    private BidStatus status;
    private LocalDateTime createdAt;

    public static BidListResponseDto from(Bid bid) {
        Integer totalMaintenanceCost = calculateTotalMaintenanceCost(bid);
        
        return BidListResponseDto.builder()
                .id(bid.getId())
                .sellerId(bid.getSeller().getSellerId())
                .sellerStoreName(bid.getSeller().getStoreName())
                .sellerRating(bid.getRatingSnapshot())
                .installmentPrincipal(bid.getInstallmentPrincipal())
                .totalMaintenanceCost(totalMaintenanceCost)
                .pricePlanName(bid.getPricePlan() != null ? bid.getPricePlan().getPlanName() : null)
                .pricePlanPrice(bid.getPricePlan() != null ? bid.getPricePlan().getPlanPrice() : null)
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    private static Integer calculateTotalMaintenanceCost(Bid bid) {
        Integer months = bid.getContractMonths() != null ? bid.getContractMonths() : 24;
        Integer planPrice = bid.getPricePlan() != null ? bid.getPricePlan().getPlanPrice() : 0;
        Integer additionalServiceTotal = bid.getAdditionalServiceList().stream()
                .mapToInt(service -> service.getServicePrice() != null ? service.getServicePrice() : 0)
                .sum();
        
        return (planPrice + additionalServiceTotal) * months;
    }
}

