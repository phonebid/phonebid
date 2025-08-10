package com.phonebid.app.trade.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.TradeErrorCode;
import com.phonebid.app.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_deliveries_contract_id", columnList = "contract_id"),
    @Index(name = "idx_deliveries_status", columnList = "status"),
    @Index(name = "idx_deliveries_invoice_number", columnList = "invoice_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(name = "courier", nullable = false)
    private Courier courier;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;



    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "delivery_memo")
    private String deliveryMemo;

    @Builder
    public Delivery(Contract contract, Courier courier, String invoiceNumber, String deliveryMemo) {
        validateDeliveryCreation(contract);
        
        this.contract = contract;
        this.courier = courier;
        this.invoiceNumber = invoiceNumber;
        this.deliveryMemo = deliveryMemo;
        this.status = DeliveryStatus.READY; // 기본값: 배송 준비
    }

    // 비즈니스 메서드
    public void ship(String invoiceNumber) {
        if (!status.canShip()) {
            throw new CustomException(TradeErrorCode.DELIVERY_CANNOT_SHIP);
        }
        
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            throw new CustomException(TradeErrorCode.MISSING_TRACKING_NUMBER);
        }
        
        this.invoiceNumber = invoiceNumber.trim();
        this.status = DeliveryStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void deliver() {
        if (!status.canDeliver()) {
            throw new CustomException(TradeErrorCode.DELIVERY_CANNOT_DELIVER);
        }
        
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public boolean hasInvoiceNumber() {
        return invoiceNumber != null && !invoiceNumber.trim().isEmpty();
    }

    public String getTrackingUrl() {
        if (!hasInvoiceNumber()) {
            throw new CustomException(TradeErrorCode.MISSING_TRACKING_NUMBER);
        }
        return courier.getTrackingUrl(invoiceNumber);
    }

    public boolean isCompleted() {
        return status.isCompleted();
    }

    public String getDeliverySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(courier.getDisplayName()).append(" - ").append(status.getDisplayName());
        
        if (hasInvoiceNumber()) {
            summary.append(" (송장: ").append(invoiceNumber).append(")");
        }
        
        return summary.toString();
    }

    public String getDeliveryProgress() {
        return switch (status) {
            case READY -> "배송 준비 중입니다.";
            case SHIPPED -> String.format("배송 중입니다. 송장번호: %s", 
                hasInvoiceNumber() ? invoiceNumber : "미등록");
            case DELIVERED -> String.format("배송이 완료되었습니다. (%s)", 
                deliveredAt.toLocalDate().toString());
        };
    }

    // 검증 메서드
    private void validateDeliveryCreation(Contract contract) {
        if (!contract.isCompleted()) {
            throw new CustomException(TradeErrorCode.INVALID_CONTRACT_STATUS);
        }
    }
} 