package com.phonebid.app.auction.repository;

import com.phonebid.app.auction.domain.Quote;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    // TODO: 필요 시 Quote 관련 커스텀 쿼리를 추가하세요.
}
