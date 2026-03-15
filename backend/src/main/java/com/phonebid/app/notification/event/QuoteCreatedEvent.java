package com.phonebid.app.notification.event;

import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.notification.domain.NotificationType;

/**
 * 견적 등록 이벤트
 * 판매자에게 새 견적 등록 알림 발송
 */
public class QuoteCreatedEvent extends NotificationEvent {
    private final Quote quote;

    public QuoteCreatedEvent(Object source, Quote quote) {
        super(source, quote.getUser().getId(), NotificationType.QUOTE_CREATED, quote.getId());
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }
}

