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
    
    /**
     * 여러 모델 ID에 해당하는 이미지들을 한 번에 조회
     * 모델 ID와 displayOrder 순서로 정렬하여 반환
     * 성능 최적화를 위해 N+1 쿼리 문제를 방지하기 위해 사용
     */
    @Query("SELECT pmi FROM PhoneModelImage pmi WHERE pmi.phoneModel.id IN :phoneModelIds ORDER BY pmi.phoneModel.id, pmi.displayOrder")
    List<PhoneModelImage> findByPhoneModelIdInOrderByDisplayOrder(@Param("phoneModelIds") List<UUID> phoneModelIds);
    
    @Modifying
    @Query("UPDATE PhoneModelImage p SET p.isDelete = true, p.deletedAt = CURRENT_TIMESTAMP WHERE p.phoneModel.id = :phoneModelId")
    void deleteByPhoneModelId(@Param("phoneModelId") UUID phoneModelId);
}

