package com.phonebid.app.notification.event;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.notification.domain.NotificationType;

/**
 * 입찰 도착 이벤트
 * 구매자에게 새 입찰 알림 발송
 */
public class BidArrivedEvent extends NotificationEvent {
    private final Bid bid;

    public BidArrivedEvent(Object source, Bid bid) {
        super(source, bid.getQuote().getUser().getId(), NotificationType.BID_ARRIVED, bid.getId());
        this.bid = bid;
    }

    public Bid getBid() {
        return bid;
    }
}

