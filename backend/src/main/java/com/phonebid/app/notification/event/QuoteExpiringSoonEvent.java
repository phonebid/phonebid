package com.phonebid.app.notification.event;

import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.notification.domain.NotificationType;

/**
 * 견적 마감 임박 이벤트
 * 구매자에게 견적 마감 임박 알림 발송
 */
public class QuoteExpiringSoonEvent extends NotificationEvent {
    private final Quote quote;

    public QuoteExpiringSoonEvent(Object source, Quote quote) {
        super(source, quote.getUser().getId(), NotificationType.QUOTE_EXPIRING_SOON, quote.getId());
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }
}

