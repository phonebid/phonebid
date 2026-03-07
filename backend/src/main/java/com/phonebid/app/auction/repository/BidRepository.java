package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.BidStatus;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
    @Query("SELECT DISTINCT b FROM Bid b " +
           "LEFT JOIN FETCH b.additionalServiceList " +
           "WHERE b.quote.id = :quoteId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.installmentPrincipal ASC")
    List<Bid> findActiveByQuoteId(@Param("quoteId") UUID quoteId, @Param("status") BidStatus status);

    /**
     * 특정 판매자의 입찰 목록 조회
     */
    @Query(value = "SELECT DISTINCT b FROM Bid b " +
           "LEFT JOIN FETCH b.additionalServiceList " +
           "WHERE b.seller.sellerId = :sellerId " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT b) FROM Bid b " +
           "WHERE b.seller.sellerId = :sellerId " +
           "AND (b.isDelete = false OR b.isDelete IS NULL)")
    Page<Bid> findBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);

    /**
     * 특정 판매자의 상태별 입찰 목록 조회
     */
    @Query(value = "SELECT DISTINCT b FROM Bid b " +
           "LEFT JOIN FETCH b.additionalServiceList " +
           "WHERE b.seller.sellerId = :sellerId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT b) FROM Bid b " +
           "WHERE b.seller.sellerId = :sellerId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL)")
    Page<Bid> findBySellerIdAndStatus(@Param("sellerId") UUID sellerId, @Param("status") BidStatus status, Pageable pageable);

    /**
     * 특정 견적의 최저 할부원금 입찰 조회
     */
    @Query("SELECT MIN(b.installmentPrincipal) FROM Bid b " +
           "WHERE b.quote.id = :quoteId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL)")
    Integer findMinInstallmentPrincipalByQuoteId(@Param("quoteId") UUID quoteId, @Param("status") BidStatus status);

    /**
     * 특정 견적과 판매자의 활성 입찰 목록 조회
     */
    @Query("SELECT DISTINCT b FROM Bid b " +
           "LEFT JOIN FETCH b.additionalServiceList " +
           "WHERE b.quote.id = :quoteId " +
           "AND b.seller.sellerId = :sellerId " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "ORDER BY b.installmentPrincipal ASC")
    List<Bid> findByQuoteIdAndSellerIdAndStatus(@Param("quoteId") UUID quoteId, 
                                                 @Param("sellerId") UUID sellerId, 
                                                 @Param("status") BidStatus status);

    /**
     * 여러 견적의 입찰 개수를 한 번에 조회 (N+1 문제 해결용)
     */
    @Query("SELECT b.quote.id as quoteId, COUNT(b) as bidCount " +
           "FROM Bid b " +
           "WHERE b.quote.id IN :quoteIds " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "GROUP BY b.quote.id")
    List<BidCountDto> countByQuoteIds(@Param("quoteIds") List<UUID> quoteIds);

    /**
     * 여러 견적의 최저 할부원금을 한 번에 조회 (N+1 문제 해결용)
     */
    @Query("SELECT b.quote.id as quoteId, MIN(b.installmentPrincipal) as minPrice " +
           "FROM Bid b " +
           "WHERE b.quote.id IN :quoteIds " +
           "AND b.status = :status " +
           "AND (b.isDelete = false OR b.isDelete IS NULL) " +
           "GROUP BY b.quote.id")
    List<BidMinPriceDto> findMinInstallmentPrincipalByQuoteIds(
            @Param("quoteIds") List<UUID> quoteIds,
            @Param("status") BidStatus status);

    /**
     * 입찰 개수 Projection 인터페이스
     */
    interface BidCountDto {
        UUID getQuoteId();
        Long getBidCount();
    }

    /**
     * 최저 할부원금 Projection 인터페이스
     */
    interface BidMinPriceDto {
        UUID getQuoteId();
        Integer getMinPrice();
    }

    /**
     * 비관적 락을 사용한 입찰 조회 (계약 생성 시 동시성 제어)
     * FOR UPDATE 쿼리로 해당 행에 배타적 락을 획득
     * 
     * @param id 입찰 ID
     * @return 비관적 락이 적용된 입찰
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bid b WHERE b.id = :id")
    Optional<Bid> findByIdWithLock(@Param("id") UUID id);
}

