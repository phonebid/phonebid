package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Bid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    /**
     * 특정 견적과 판매자에 대한 입찰 정보 조회
     */
    @Query("SELECT b FROM Bid b WHERE b.quote.id = :quoteId AND b.seller.sellerId = :sellerId ORDER BY b.createdAt DESC")
    Optional<Bid> findLatestByQuoteIdAndSellerId(@Param("quoteId") UUID quoteId, @Param("sellerId") UUID sellerId);

    /**
     * 특정 견적에 대한 입찰 개수 조회
     */
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.quote.id = :quoteId AND (b.isDelete = false OR b.isDelete IS NULL)")
    long countByQuoteId(@Param("quoteId") UUID quoteId);
}

