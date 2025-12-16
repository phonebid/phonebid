package com.phonebid.app.customerservice.repository;

import com.phonebid.app.customerservice.domain.Inquiry;
import com.phonebid.app.customerservice.domain.InquiryCategory;
import com.phonebid.app.customerservice.domain.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {

    @Query("SELECT i FROM Inquiry i WHERE i.user.username = :username " +
           "AND (i.isDelete = false OR i.isDelete IS NULL) ORDER BY i.createdAt DESC")
    Page<Inquiry> findByUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE i.id = :id AND i.user.username = :username " +
           "AND (i.isDelete = false OR i.isDelete IS NULL)")
    Optional<Inquiry> findByIdAndUsername(@Param("id") UUID id, @Param("username") String username);

    @Query("SELECT i FROM Inquiry i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(i.isDelete = false OR i.isDelete IS NULL) " +
           "ORDER BY i.createdAt DESC")
    Page<Inquiry> findAllWithFilters(@Param("status") InquiryStatus status, @Param("category") InquiryCategory category, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE i.id = :id " +
           "AND (i.isDelete = false OR i.isDelete IS NULL)")
    Optional<Inquiry> findByIdAndNotDeleted(@Param("id") UUID id);
}

