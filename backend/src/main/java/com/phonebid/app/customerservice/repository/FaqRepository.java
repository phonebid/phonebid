package com.phonebid.app.customerservice.repository;

import com.phonebid.app.customerservice.domain.Faq;
import com.phonebid.app.customerservice.domain.FaqCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaqRepository extends JpaRepository<Faq, UUID> {

    @Query("SELECT f FROM Faq f WHERE (:category IS NULL OR f.category = :category) " +
           "AND (f.isDelete = false OR f.isDelete IS NULL) ORDER BY f.createdAt DESC")
    Page<Faq> findAllWithCategoryFilter(@Param("category") FaqCategory category, Pageable pageable);

    @Query("SELECT f FROM Faq f WHERE f.id = :id AND (f.isDelete = false OR f.isDelete IS NULL)")
    Optional<Faq> findByIdForView(@Param("id") UUID id);
}

