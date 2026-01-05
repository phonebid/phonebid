package com.phonebid.app.phone.repository;

import com.phonebid.app.phone.domain.PhoneModelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhoneModelImageRepository extends JpaRepository<PhoneModelImage, UUID> {
    List<PhoneModelImage> findByPhoneModelIdOrderByDisplayOrder(UUID phoneModelId);
    
    @Modifying
    @Query("UPDATE PhoneModelImage p SET p.isDelete = true, p.deletedAt = CURRENT_TIMESTAMP WHERE p.phoneModel.id = :phoneModelId")
    void deleteByPhoneModelId(@Param("phoneModelId") UUID phoneModelId);
}

