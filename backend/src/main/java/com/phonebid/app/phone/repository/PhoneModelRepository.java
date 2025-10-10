package com.phonebid.app.phone.repository;

import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * PhoneModel Repository
 * 휴대폰 모델 데이터 접근을 위한 인터페이스
 */
@Repository
public interface PhoneModelRepository extends JpaRepository<PhoneModel, UUID> {


    /**
     * 브랜드와 모델명으로 모델 조회
     */
    Optional<PhoneModel> findByBrandAndModel(@Param("brand") Brand brand, @Param("model") String model);

}
