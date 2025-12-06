package com.phonebid.app.auction.dto.request;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.BidAdditionalService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdditionalServiceRequestDto {

    @NotBlank(message = "부가서비스 이름은 필수입니다.")
    private String serviceName;

    @NotNull(message = "부가서비스 가격은 필수입니다.")
    @Min(value = 0, message = "부가서비스 가격은 0 이상이어야 합니다.")
    private Integer servicePrice;

    private String description;

    private Boolean mandatory;

    private Integer cancellableAfterMonths;

    @Builder
    public AdditionalServiceRequestDto(String serviceName, Integer servicePrice, String description,
                                        Boolean mandatory, Integer cancellableAfterMonths) {
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.description = description;
        this.mandatory = mandatory;
        this.cancellableAfterMonths = cancellableAfterMonths;
    }

    public BidAdditionalService toEntity(Bid bid) {
        return BidAdditionalService.builder()
                .bid(bid)
                .serviceName(serviceName)
                .servicePrice(servicePrice)
                .description(description)
                .mandatory(mandatory)
                .cancellableAfterMonths(cancellableAfterMonths)
                .build();
    }
}

