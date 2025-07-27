package com.phonebid.app.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phonebid.app.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String providerId);
}
