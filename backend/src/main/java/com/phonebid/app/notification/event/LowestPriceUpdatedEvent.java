package com.phonebid.app.notification.event;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.notification.domain.NotificationType;

/**
 * 최저가 갱신 이벤트
 * 판매자에게 최저가 갱신 알림 발송 (역경매 특성)
 */
public class LowestPriceUpdatedEvent extends NotificationEvent {
    private final Bid bid;
    private final Integer previousLowestPrice;

    public LowestPriceUpdatedEvent(Object source, Bid bid, Integer previousLowestPrice) {
        super(source, bid.getQuote().getUser().getId(), NotificationType.LOWEST_PRICE_UPDATED, bid.getId());
        this.bid = bid;
        this.previousLowestPrice = previousLowestPrice;
    }

    public Bid getBid() {
        return bid;
    }

    public Integer getPreviousLowestPrice() {
        return previousLowestPrice;
    }
}

