package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PricePlan;
import com.phonebid.app.auction.domain.PricePlanCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricePlanRepository extends JpaRepository<PricePlan, UUID> {

    @Query("SELECT pp FROM PricePlan pp WHERE pp.id = :id AND pp.isActive = true AND (pp.isDelete = false OR pp.isDelete IS NULL)")
    Optional<PricePlan> findByIdAndIsActiveTrue(@Param("id") UUID id);

    @Query("SELECT pp FROM PricePlan pp WHERE pp.isActive = true AND (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.carrier, pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findAllActiveOrderByCarrierAndDisplayOrder();

    @Query("SELECT pp FROM PricePlan pp WHERE pp.carrier = :carrier AND pp.isActive = true AND (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findByCarrierAndIsActiveTrue(@Param("carrier") Carrier carrier);

    @Query("SELECT pp FROM PricePlan pp WHERE pp.category = :category AND pp.isActive = true AND (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.carrier, pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findByCategoryAndIsActiveTrue(@Param("category") PricePlanCategory category);

    @Query("SELECT pp FROM PricePlan pp WHERE pp.carrier = :carrier AND pp.category = :category AND pp.isActive = true AND (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findByCarrierAndCategoryAndIsActiveTrue(@Param("carrier") Carrier carrier, @Param("category") PricePlanCategory category);

    @Query("SELECT pp FROM PricePlan pp WHERE (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.carrier, pp.category, pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findAllForAdmin();

    @Query("SELECT pp FROM PricePlan pp WHERE pp.carrier = :carrier AND (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.category, pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findByCarrierForAdmin(@Param("carrier") Carrier carrier);

    @Query("SELECT pp FROM PricePlan pp WHERE pp.carrier = :carrier AND pp.monthlyFee <= :maxPrice AND pp.isActive = true AND (pp.isDelete = false OR pp.isDelete IS NULL) ORDER BY pp.displayOrder, pp.monthlyFee DESC")
    List<PricePlan> findByCarrierAndMaxPriceAndIsActiveTrue(@Param("carrier") Carrier carrier, @Param("maxPrice") Integer maxPrice);

    boolean existsByCarrierAndPlanName(Carrier carrier, String planName);
}

