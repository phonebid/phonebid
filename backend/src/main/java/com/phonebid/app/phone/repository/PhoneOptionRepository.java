package com.phonebid.app.phone.repository;

import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.domain.PhoneOption.OptionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PhoneOption Repository
 * 휴대폰 옵션 데이터 접근을 위한 인터페이스
 */
@Repository
public interface PhoneOptionRepository extends JpaRepository<PhoneOption, UUID> {

    /**
     * 모델 ID로 옵션 목록 조회
     */
    List<PhoneOption> findByModelId(@Param("modelId") UUID modelId);

    /**
     * 모델 ID와 옵션 타입으로 옵션 조회
     */
    List<PhoneOption> findByModelIdAndOptionType(@Param("modelId") UUID modelId, @Param("optionType") OptionType optionType);


    /**
     * 모델 ID, 옵션 타입, 옵션 값으로 중복 검증
     */
    boolean existsByModelIdAndOptionTypeAndOptionValue(@Param("modelId") UUID modelId, @Param("optionType") OptionType optionType, @Param("optionValue") String optionValue);

    /**
     * 모델 ID, 옵션 타입, 옵션 값으로 옵션 조회
     */
    Optional<PhoneOption> findByModelIdAndOptionTypeAndOptionValue(@Param("modelId") UUID modelId, @Param("optionType") OptionType optionType, @Param("optionValue") String optionValue);

    /**
     * 모델별 색상 옵션만 조회
     */
    List<PhoneOption> findColorOptionsByModelId(@Param("modelId") UUID modelId);

    /**
     * 모델별 저장용량 옵션만 조회
     */
    List<PhoneOption> findStorageOptionsByModelId(@Param("modelId") UUID modelId);


    /**
     * 모델별 옵션 개수 조회
     */
    long countByModelId(@Param("modelId") UUID modelId);

    /**
     * 모델별 옵션 타입별 개수 조회
     */
    long countByModelIdAndOptionType(@Param("modelId") UUID modelId, @Param("optionType") OptionType optionType);
}
