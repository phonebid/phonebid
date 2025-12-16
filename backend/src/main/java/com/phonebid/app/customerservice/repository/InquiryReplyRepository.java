package com.phonebid.app.customerservice.repository;

import com.phonebid.app.customerservice.domain.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InquiryReplyRepository extends JpaRepository<InquiryReply, UUID> {

    @Query("SELECT ir FROM InquiryReply ir JOIN FETCH ir.admin WHERE ir.inquiry.id = :inquiryId " +
           "AND (ir.isDelete = false OR ir.isDelete IS NULL)")
    Optional<InquiryReply> findByInquiryId(@Param("inquiryId") UUID inquiryId);

    @Query("SELECT ir FROM InquiryReply ir WHERE ir.id = :id " +
           "AND (ir.isDelete = false OR ir.isDelete IS NULL)")
    Optional<InquiryReply> findByIdAndNotDeleted(@Param("id") UUID id);
}

