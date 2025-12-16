package com.phonebid.app.mypage.repository;

import com.phonebid.app.mypage.domain.Account;
import com.phonebid.app.mypage.domain.Bank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * 사용자명으로 계좌 목록을 페이징 조회
     * 등록일 기준 내림차순으로 정렬하여 반환
     */
    @Query(value = "SELECT a FROM Account a " +
           "JOIN FETCH a.user u " +
           "WHERE u.username = :username " +
           "AND (a.isDelete = false OR a.isDelete IS NULL) " +
           "ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Account a " +
           "JOIN a.user u " +
           "WHERE u.username = :username " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    Page<Account> findByUsername(@Param("username") String username, Pageable pageable);

    /**
     * 계좌 ID와 사용자명으로 특정 계좌를 조회
     * 본인의 계좌만 조회할 수 있도록 사용자명으로 필터링
     */
    @Query("SELECT a FROM Account a " +
           "JOIN FETCH a.user u " +
           "WHERE a.id = :accountId " +
           "AND u.username = :username " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    Optional<Account> findByIdAndUsername(@Param("accountId") UUID accountId, @Param("username") String username);

    /**
     * 사용자명, 계좌번호, 은행으로 계좌 중복 여부 확인
     * 동일 사용자의 동일 은행, 동일 계좌번호 조합이 이미 존재하는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a " +
           "JOIN a.user u " +
           "WHERE u.username = :username " +
           "AND a.accountNumber = :accountNumber " +
           "AND a.bank = :bank " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    boolean existsByUsernameAndAccountNumberAndBank(
            @Param("username") String username,
            @Param("accountNumber") String accountNumber,
            @Param("bank") Bank bank);
}

