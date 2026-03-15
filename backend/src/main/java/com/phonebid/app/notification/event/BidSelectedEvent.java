package com.phonebid.app.notification.event;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.notification.domain.NotificationType;

/**
 * 입찰 선택 이벤트
 * 판매자에게 입찰 선택 알림 발송
 */
public class BidSelectedEvent extends NotificationEvent {
    private final Bid bid;

    public BidSelectedEvent(Object source, Bid bid) {
        super(source, bid.getSeller().getUser().getId(), NotificationType.BID_SELECTED, bid.getId());
        this.bid = bid;
    }

    public Bid getBid() {
        return bid;
    }
}

