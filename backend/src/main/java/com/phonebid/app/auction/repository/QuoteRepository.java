package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import java.util.List;
import java.util.UUID;
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
            "SELECT q FROM Quote q "
                    + "WHERE q.user.id = :userId "
                    + "AND q.status = :status "
                    + "AND (q.isDelete = false OR q.isDelete IS NULL) "
                    + "ORDER BY q.createdAt DESC")
    List<Quote> findByUserIdAndStatus(
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
}

