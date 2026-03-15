package com.phonebid.app.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND (u.isDelete = false OR u.isDelete IS NULL)")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND (u.isDelete = false OR u.isDelete IS NULL)")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND (u.isDelete = false OR u.isDelete IS NULL)")
    Optional<User> findByNickname(@Param("nickname") String nickname);

    @Query("SELECT u FROM User u WHERE u.providerId = :providerId AND (u.isDelete = false OR u.isDelete IS NULL)")
    Optional<User> findByProviderId(@Param("providerId") String providerId);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (u.isDelete = false OR u.isDelete IS NULL)")
    List<User> findByRole(@Param("role") Role role);
}
