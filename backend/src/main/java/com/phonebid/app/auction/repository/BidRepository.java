package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.BidStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    /**
     * 특정 견적과 판매자에 대한 최신 입찰 정보 조회
     * 
     * 수정 이유:
     * - 기존 @Query 방식은 ORDER BY만 있고 LIMIT 1 제약이 없어서,
     *   동일 견적·동일 판매자에 대해 여러 입찰이 존재할 경우 NonUniqueResultException 발생 가능
     * - Spring Data 메서드 이름 규칙의 findFirst를 사용하여 자동으로 첫 번째 결과만 반환하도록 개선
     * - findFirst + OrderByCreatedAtDesc 조합으로 최신 입찰 1건만 안전하게 조회
     */
    Optional<Bid> findFirstByQuote_IdAndSeller_SellerIdOrderByCreatedAtDesc(UUID quoteId, UUID sellerId);

    /**
     * 특정 견적에 대한 입찰 개수 조회
     */
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.quote.id = :quoteId AND (b.isDelete = false OR b.isDelete IS NULL)")
    long countByQuoteId(@Param("quoteId") UUID quoteId);

    /**
     * 특정 견적과 판매자에 대한 중복 입찰 체크
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bid b " +
           "WHERE b.quote.id = :quoteId AND b.seller.sellerId = :sellerId " +
           "AND (b.isDelete = false OR b.isDelete IS NULL)")
    boolean existsByQuoteIdAndSellerId(@Param("quoteId") UUID quoteId, @Param("sellerId") UUID sellerId);

    /**
     * 특정 견적의 모든 입찰 목록 조회 (할부원금 기준 정렬)
     */
    @Query("SELECT b FROM Bid b " +
           "WHERE b.quote.id = :quoteId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.installmentPrincipal ASC")
    List<Bid> findByQuoteIdAndStatus(@Param("quoteId") UUID quoteId, @Param("status") BidStatus status);

    /**
     * 특정 견적의 모든 활성 입찰 목록 조회
     */
    @Query("SELECT b FROM Bid b " +
           "WHERE b.quote.id = :quoteId " +
           "AND b.status = 'ACTIVE' " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.installmentPrincipal ASC")
    List<Bid> findActiveByQuoteId(@Param("quoteId") UUID quoteId);

    /**
     * 특정 판매자의 입찰 목록 조회
     */
    @Query("SELECT b FROM Bid b " +
           "WHERE b.seller.sellerId = :sellerId " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.createdAt DESC")
    List<Bid> findBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);

    /**
     * 특정 판매자의 상태별 입찰 목록 조회
     */
    @Query("SELECT b FROM Bid b " +
           "WHERE b.seller.sellerId = :sellerId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.createdAt DESC")
    List<Bid> findBySellerIdAndStatus(@Param("sellerId") UUID sellerId, @Param("status") BidStatus status, Pageable pageable);

    /**
     * 특정 견적의 최저 할부원금 입찰 조회
     */
    @Query("SELECT MIN(b.installmentPrincipal) FROM Bid b " +
           "WHERE b.quote.id = :quoteId " +
           "AND b.status = 'ACTIVE' " +
           "AND (b.isDelete = false OR b.isDelete IS NULL)")
    Integer findMinInstallmentPrincipalByQuoteId(@Param("quoteId") UUID quoteId);
}

