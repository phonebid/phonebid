package com.phonebid.app.trade.service;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.errorcode.TradeErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.event.BidSelectedEvent;
import com.phonebid.app.trade.domain.Contract;
import com.phonebid.app.trade.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 계약 서비스
 * 입찰 선택과 계약 생성을 트랜잭션으로 통합 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final QuoteRepository quoteRepository;
    private final BidRepository bidRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 계약 생성 (입찰 선택 포함)
     * 비관적 락을 사용하여 동시성 문제 방지 (중복 클릭, 멀티탭 등)
     * 
     * @param quoteId 견적 ID
     * @param bidId 선택할 입찰 ID
     * @param user 계약 생성 요청자 (소비자)
     * @return 생성된 계약
     */
    @Transactional
    public Contract createContract(UUID quoteId, UUID bidId, User user) {
        // 1. 비관적 락으로 견적 조회 (FOR UPDATE)
        Quote quote = quoteRepository.findByIdWithLock(quoteId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));

        // 2. 견적 소유자 확인
        if (!quote.getUser().getId().equals(user.getId())) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_OWNED_BY_USER);
        }

        // 3. 이미 계약이 존재하는지 확인 (락 획득 후 이중 체크)
        if (contractRepository.existsByQuoteId(quoteId)) {
            throw new CustomException(TradeErrorCode.CONTRACT_ALREADY_EXISTS);
        }

        // 4. 견적 상태 확인 (락으로 보호된 상태에서)
        if (!quote.getStatus().isOpen()) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }

        // 5. 비관적 락으로 입찰 조회
        Bid bid = bidRepository.findByIdWithLock(bidId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.BID_NOT_FOUND));

        // 6. 입찰이 해당 견적에 속하는지 확인
        if (!bid.getQuote().getId().equals(quoteId)) {
            throw new CustomException(TradeErrorCode.INVALID_BID_FOR_QUOTE);
        }

        // 7. 입찰을 SELECTED 상태로 변경
        bid.select();
        bidRepository.save(bid);

        // 8. 견적 상태를 CONTRACTED로 변경
        quote.markContracted();
        quoteRepository.save(quote);

        // 9. 계약 생성
        Contract contract = Contract.builder()
                .quote(quote)
                .selectedBid(bid)
                .build();
        Contract savedContract = contractRepository.save(contract);

        log.info("계약 생성 완료 - contractId: {}, quoteId: {}, bidId: {}, userId: {}",
                savedContract.getId(), quoteId, bidId, user.getId());

        // 10. 입찰 선택 이벤트 발행
        eventPublisher.publishEvent(new BidSelectedEvent(this, bid));

        return savedContract;
    }
}

