package com.phonebid.app.trade.repository;

import com.phonebid.app.trade.domain.Payment;
import com.phonebid.app.trade.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 결제(Payment) 엔티티에 대한 데이터 접근을 담당하는 Repository
 * 계약별 결제 정보 조회 기능을 제공합니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * 계약 ID로 결제 정보를 조회합니다.
     * 계약에 연결된 결제 정보를 가져올 때 사용됩니다.
     */
    @Query("SELECT p FROM Payment p " +
           "JOIN FETCH p.contract c " +
           "WHERE c.id = :contractId " +
           "AND (p.isDelete = false OR p.isDelete IS NULL)")
    Optional<Payment> findByContractId(@Param("contractId") UUID contractId);

    /**
     * 계약 ID와 결제 상태로 결제 정보를 조회합니다.
     * 특정 상태의 결제만 조회할 때 사용됩니다.
     */
    @Query("SELECT p FROM Payment p " +
           "JOIN FETCH p.contract c " +
           "WHERE c.id = :contractId " +
           "AND p.status = :status " +
           "AND (p.isDelete = false OR p.isDelete IS NULL)")
    Optional<Payment> findByContractIdAndStatus(
            @Param("contractId") UUID contractId,
            @Param("status") PaymentStatus status);
}

