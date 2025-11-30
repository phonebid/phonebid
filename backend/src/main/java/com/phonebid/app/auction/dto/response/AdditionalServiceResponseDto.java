package com.phonebid.app.auction.dto.response;

import com.phonebid.app.auction.domain.BidAdditionalService;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdditionalServiceResponseDto {

    private UUID id;
    private String serviceName;
    private Integer servicePrice;
    private String description;
    private Boolean mandatory;
    private Integer cancellableAfterMonths;
    private String cancellableDescription;

    public static AdditionalServiceResponseDto from(BidAdditionalService additionalService) {
        return AdditionalServiceResponseDto.builder()
                .id(additionalService.getId())
                .serviceName(additionalService.getServiceName())
                .servicePrice(additionalService.getServicePrice())
                .description(additionalService.getDescription())
                .mandatory(additionalService.isMandatory())
                .cancellableAfterMonths(additionalService.getCancellableAfterMonths())
                .cancellableDescription(additionalService.getCancellableDescription())
                .build();
    }
}

