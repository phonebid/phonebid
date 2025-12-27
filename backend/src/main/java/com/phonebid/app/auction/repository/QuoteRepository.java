package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    @Query(
            "SELECT q FROM Quote q "
                    + "WHERE q.status = :status "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL) "
                    + "ORDER BY q.createdAt DESC")
    List<Quote> findLatestQuotesByStatus(
            @Param("status") QuoteStatus status);

    @Query(
            value = "SELECT q FROM Quote q "
                    + "WHERE q.user.id = :userId "
                    + "AND q.status = :status "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL) "
                    + "ORDER BY q.createdAt DESC",
            countQuery = "SELECT COUNT(q) FROM Quote q "
                    + "WHERE q.user.id = :userId "
                    + "AND q.status = :status "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL)")
    Page<Quote> findByUserIdAndStatus(
            @Param("userId") UUID userId, 
            @Param("status") QuoteStatus status, 
            Pageable pageable);

    @Query(
            "SELECT q FROM Quote q "
                    + "WHERE q.status = :status "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL) "
                    + "AND (:modelId IS NULL OR q.phoneModel.id = :modelId) "
                    + "ORDER BY q.createdAt DESC")
    List<Quote> findLatestQuotesByStatusAndModel(
            @Param("status") QuoteStatus status,
            @Param("modelId") UUID modelId,
            Pageable pageable);

    /**
     * 완료된 견적 조회 (진행중인 견적과 완료된 견적을 함께 조회할 때 사용됨)
     * 사용자 ID와 상태 목록으로 견적 목록을 페이징 조회
     */
    @Query(
            value = "SELECT q FROM Quote q "
                    + "WHERE q.user.id = :userId "
                    + "AND q.status IN :statuses "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL) "
                    + "ORDER BY q.createdAt DESC",
            countQuery = "SELECT COUNT(q) FROM Quote q "
                    + "WHERE q.user.id = :userId "
                    + "AND q.status IN :statuses "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL)")
    Page<Quote> findByUserIdAndStatusIn(@Param("userId") UUID userId,
                                        @Param("statuses") List<QuoteStatus> statuses,
                                        Pageable pageable);
}

