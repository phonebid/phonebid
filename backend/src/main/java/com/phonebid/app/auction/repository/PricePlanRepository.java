package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PricePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// 요금제 Repository
public interface PricePlanRepository extends JpaRepository<PricePlan, UUID> {

    /**
     * 통신사별 요금제 목록 조회
     */
    @Query("SELECT pp FROM PricePlan pp WHERE pp.carrier = :carrier AND (pp.isDelete = false OR pp.isDelete IS NULL)")
    List<PricePlan> findByCarrier(@Param("carrier") Carrier carrier);

    /**
     * 통신사 및 최대 가격 조건으로 요금제 목록 조회
     */
    @Query("SELECT pp FROM PricePlan pp WHERE pp.carrier = :carrier AND pp.planPrice <= :maxPrice AND (pp.isDelete = false OR pp.isDelete IS NULL)")
    List<PricePlan> findByCarrierAndMaxPrice(@Param("carrier") Carrier carrier, @Param("maxPrice") Integer maxPrice);
}

