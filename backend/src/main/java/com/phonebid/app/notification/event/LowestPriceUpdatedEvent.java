package com.phonebid.app.notification.event;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.notification.domain.NotificationType;

import java.util.UUID;

/**
 * 최저가 갱신 이벤트
 * 구매자와 판매자 모두에게 최저가 갱신 알림 발송
 */
public class LowestPriceUpdatedEvent extends NotificationEvent {
    private final Bid bid;
    private final Integer previousLowestPrice;
    private final UUID sellerUserId;

    public LowestPriceUpdatedEvent(Object source, Bid bid, Integer previousLowestPrice) {
        super(source, bid.getQuote().getUser().getId(), NotificationType.LOWEST_PRICE_UPDATED, bid.getId());
        this.bid = bid;
        this.previousLowestPrice = previousLowestPrice;
        this.sellerUserId = bid.getSeller().getUser().getId();
    }

    public Bid getBid() {
        return bid;
    }

    public Integer getPreviousLowestPrice() {
        return previousLowestPrice;
    }

    public UUID getSellerUserId() {
        return sellerUserId;
    }
}

