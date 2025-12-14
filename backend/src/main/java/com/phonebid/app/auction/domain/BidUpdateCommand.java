package com.phonebid.app.auction.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 입찰 정보 수정을 위한 커맨드 객체
 * 도메인 레이어에서 사용하는 불변 객체
 */
@Getter
@Builder
public class BidUpdateCommand {
    private final Integer price;
    private final Integer deliveryDays;
    private final Integer additionalSubsidy;
    private final Integer installmentPrincipal;
    private final Integer contractMonths;

    public boolean hasPrice() {
        return price != null;
    }

    public boolean hasDeliveryDays() {
        return deliveryDays != null;
    }

    public boolean hasAdditionalSubsidy() {
        return additionalSubsidy != null;
    }

    public boolean hasInstallmentPrincipal() {
        return installmentPrincipal != null;
    }

    public boolean hasContractMonths() {
        return contractMonths != null;
    }
}

