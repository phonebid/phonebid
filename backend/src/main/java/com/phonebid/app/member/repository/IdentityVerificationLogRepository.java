package com.phonebid.app.member.repository;

import com.phonebid.app.member.domain.IdentityVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdentityVerificationLogRepository extends JpaRepository<IdentityVerificationLog, UUID> {
    Optional<IdentityVerificationLog> findFirstByCi(String ci);
}
