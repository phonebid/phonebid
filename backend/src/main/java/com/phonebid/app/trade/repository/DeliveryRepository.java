package com.phonebid.app.trade.repository;

import com.phonebid.app.trade.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    /**
     * 계약 ID로 배송 정보를 조회
     * 계약에 연결된 배송 정보(송장번호, 배송 상태 등)를 가져올 때 사용됨
     */
    @Query("SELECT d FROM Delivery d " +
           "JOIN FETCH d.contract c " +
           "WHERE c.id = :contractId " +
           "AND (d.isDelete = false OR d.isDelete IS NULL)")
    Optional<Delivery> findByContractId(@Param("contractId") UUID contractId);
}

