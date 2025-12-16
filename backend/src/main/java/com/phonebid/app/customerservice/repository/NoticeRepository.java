package com.phonebid.app.customerservice.repository;

import com.phonebid.app.customerservice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {

    @Query("SELECT n FROM Notice n ORDER BY n.isImportant DESC, n.createdAt DESC")
    Page<Notice> findAllOrdered(Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.id = :id")
    Optional<Notice> findByIdForView(@Param("id") UUID id);
}

