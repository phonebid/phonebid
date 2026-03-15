package com.phonebid.app.notification.event;

import com.phonebid.app.notification.domain.NotificationType;
import com.phonebid.app.trade.domain.Contract;

import java.util.UUID;

/**
 * 계약 체결 이벤트
 * 구매자와 판매자 모두에게 계약 체결 알림 발송
 */
public class ContractSignedEvent extends NotificationEvent {
    private final Contract contract;
    private final UUID sellerUserId;

    public ContractSignedEvent(Object source, Contract contract) {
        super(source, contract.getQuote().getUser().getId(), NotificationType.CONTRACT_SIGNED, contract.getId());
        this.contract = contract;
        this.sellerUserId = contract.getSelectedBid().getSeller().getUser().getId();
    }

    public Contract getContract() {
        return contract;
    }

    public UUID getSellerUserId() {
        return sellerUserId;
    }
}

