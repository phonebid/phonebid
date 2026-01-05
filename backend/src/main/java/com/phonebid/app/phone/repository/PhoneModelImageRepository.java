package com.phonebid.app.phone.repository;

import com.phonebid.app.phone.domain.PhoneModelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhoneModelImageRepository extends JpaRepository<PhoneModelImage, UUID> {
    List<PhoneModelImage> findByPhoneModelIdOrderByDisplayOrder(UUID phoneModelId);
    void deleteByPhoneModelId(UUID phoneModelId);
}

