package com.phonebid.app.trade.repository;

import com.phonebid.app.trade.domain.Contract;
import com.phonebid.app.trade.domain.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    /**
     * 사용자명과 계약 상태로 계약 목록을 페이징 조회
     * 구매완료(SIGNED) 또는 취소(CANCELLED) 상태의 계약을 조회할 때 사용됨
     */
    @Query(value = "SELECT c FROM Contract c " +
           "JOIN FETCH c.quote q " +
           "JOIN FETCH q.user u " +
           "JOIN FETCH c.selectedBid b " +
           "JOIN FETCH q.phoneModel pm " +
           "WHERE u.username = :username " +
           "AND c.status = :status " +
           "AND (c.isDelete = false OR c.isDelete IS NULL) " +
           "ORDER BY c.signedAt DESC",
           countQuery = "SELECT COUNT(c) FROM Contract c " +
           "JOIN c.quote q " +
           "JOIN q.user u " +
           "WHERE u.username = :username " +
           "AND c.status = :status " +
           "AND (c.isDelete = false OR c.isDelete IS NULL)")
    Page<Contract> findByUsernameAndStatus(@Param("username") String username, @Param("status") ContractStatus status, Pageable pageable);

    /**
     * 계약 ID와 사용자명으로 특정 계약을 조회
     * 구매내역 상세 조회 시 사용되며, 본인의 계약만 조회할 수 있도록 사용자명으로 필터링함
     */
    @Query("SELECT c FROM Contract c " +
           "JOIN FETCH c.quote q " +
           "JOIN FETCH q.user u " +
           "JOIN FETCH c.selectedBid b " +
           "JOIN FETCH q.phoneModel pm " +
           "LEFT JOIN FETCH q.storage " +
           "LEFT JOIN FETCH q.color " +
           "WHERE c.id = :contractId " +
           "AND u.username = :username " +
           "AND (c.isDelete = false OR c.isDelete IS NULL)")
    Optional<Contract> findByIdAndUsername(@Param("contractId") UUID contractId, @Param("username") String username);

    /**
     * 사용자명과 상태 목록으로 계약 목록을 페이징 조회
     * 구매완료와 취소를 함께 조회할 때 사용됨
     */
    @Query(value = "SELECT c FROM Contract c " +
           "JOIN FETCH c.quote q " +
           "JOIN FETCH q.user u " +
           "JOIN FETCH c.selectedBid b " +
           "JOIN FETCH q.phoneModel pm " +
           "WHERE u.username = :username " +
           "AND c.status IN :statuses " +
           "AND (c.isDelete = false OR c.isDelete IS NULL) " +
           "ORDER BY c.signedAt DESC",
           countQuery = "SELECT COUNT(c) FROM Contract c " +
           "JOIN c.quote q " +
           "JOIN q.user u " +
           "WHERE u.username = :username " +
           "AND c.status IN :statuses " +
           "AND (c.isDelete = false OR c.isDelete IS NULL)")
    Page<Contract> findAllByUsername(@Param("username") String username, @Param("statuses") List<ContractStatus> statuses, Pageable pageable);
}

