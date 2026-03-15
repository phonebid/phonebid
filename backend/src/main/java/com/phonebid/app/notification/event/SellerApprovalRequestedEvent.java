package com.phonebid.app.notification.event;

import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.notification.domain.NotificationType;

/**
 * 판매자 승인 요청 이벤트
 * 관리자에게 판매자 승인 요청 알림 발송
 */
public class SellerApprovalRequestedEvent extends NotificationEvent {
    private final Seller seller;

    public SellerApprovalRequestedEvent(Object source, Seller seller) {
        super(source, null, NotificationType.SELLER_APPROVAL_REQUESTED, seller.getSellerId());
        this.seller = seller;
    }

    public Seller getSeller() {
        return seller;
    }
}

