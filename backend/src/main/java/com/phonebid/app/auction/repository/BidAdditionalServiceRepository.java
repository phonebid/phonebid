package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.BidAdditionalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// 입찰 부가서비스 Repository
public interface BidAdditionalServiceRepository extends JpaRepository<BidAdditionalService, UUID> {

    /**
     * 특정 입찰의 부가서비스 목록 조회
     */
    @Query("SELECT bas FROM BidAdditionalService bas WHERE bas.bid.id = :bidId AND (bas.isDelete = false OR bas.isDelete IS NULL)")
    List<BidAdditionalService> findByBidId(@Param("bidId") UUID bidId);

    /**
     * 특정 입찰의 부가서비스 총 금액 조회
     */
    @Query("SELECT SUM(bas.servicePrice) FROM BidAdditionalService bas WHERE bas.bid.id = :bidId AND (bas.isDelete = false OR bas.isDelete IS NULL)")
    Integer getTotalServicePriceByBidId(@Param("bidId") UUID bidId);
}

