package com.phonebid.app.member.repository;

import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 판매자 Repository
 * 판매자 데이터 접근을 위한 인터페이스
 */
@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {

    /**
     * username으로 판매자 조회
     */
    @Query("SELECT s FROM Seller s JOIN s.user u WHERE u.username = :username")
    Optional<Seller> findByUsername(@Param("username") String username);

    /**
     * username으로 판매자 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM Seller s JOIN s.user u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 사업자등록번호로 판매자 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM Seller s WHERE s.businessNumber = :businessNumber")
    boolean existsByBusinessNumber(@Param("businessNumber") String businessNumber);

    /**
     * 사업자등록번호로 판매자 조회
     */
    Optional<Seller> findByBusinessNumber(String businessNumber);

    /**
     * 승인 상태별 판매자 목록 조회
     */
    List<Seller> findByApprovalStatus(ApprovalStatus approvalStatus);

    /**
     * 승인된 판매자 목록 조회
     */
    @Query("SELECT s FROM Seller s WHERE s.approvalStatus = 'APPROVED'")
    List<Seller> findApprovedSellers();

    /**
     * User ID로 판매자 조회
     */
    @Query("SELECT s FROM Seller s WHERE s.user.id = :userId")
    Optional<Seller> findByUserId(@Param("userId") UUID userId);
} 