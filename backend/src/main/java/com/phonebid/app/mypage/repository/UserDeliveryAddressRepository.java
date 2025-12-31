package com.phonebid.app.mypage.repository;

import com.phonebid.app.mypage.domain.UserDeliveryAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserDeliveryAddressRepository extends JpaRepository<UserDeliveryAddress, UUID> {

    /**
     * 사용자명으로 배송지 목록을 페이징 조회
     * 등록일 기준 내림차순으로 정렬하여 반환
     */
    @Query(value = "SELECT a FROM UserDeliveryAddress a " +
           "JOIN FETCH a.user u " +
           "WHERE u.username = :username " +
           "AND (a.isDelete = false OR a.isDelete IS NULL) " +
           "ORDER BY a.isDefault DESC, a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM UserDeliveryAddress a " +
           "JOIN a.user u " +
           "WHERE u.username = :username " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    Page<UserDeliveryAddress> findByUsername(@Param("username") String username, Pageable pageable);

    /**
     * 사용자의 기본 배송지 조회
     */
    @Query("SELECT a FROM UserDeliveryAddress a " +
           "JOIN FETCH a.user u " +
           "WHERE u.username = :username " +
           "AND a.isDefault = true " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    Optional<UserDeliveryAddress> findDefaultByUsername(@Param("username") String username);

    /**
     * 배송지 ID와 사용자명으로 특정 배송지를 조회
     * 본인의 배송지만 조회할 수 있도록 사용자명으로 필터링
     */
    @Query("SELECT a FROM UserDeliveryAddress a " +
           "JOIN FETCH a.user u " +
           "WHERE a.id = :addressId " +
           "AND u.username = :username " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    Optional<UserDeliveryAddress> findByIdAndUsername(@Param("addressId") UUID addressId, @Param("username") String username);

    /**
     * 사용자의 기본 배송지가 있는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM UserDeliveryAddress a " +
           "JOIN a.user u " +
           "WHERE u.username = :username " +
           "AND a.isDefault = true " +
           "AND (a.isDelete = false OR a.isDelete IS NULL)")
    boolean existsDefaultByUsername(@Param("username") String username);
}

