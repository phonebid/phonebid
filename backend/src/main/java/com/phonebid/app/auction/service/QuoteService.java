package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.BidStatus;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.dto.response.QuoteResponseDto;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.errorcode.PhoneErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.repository.PhoneModelRepository;
import com.phonebid.app.notification.event.QuoteCreatedEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final PhoneModelRepository phoneModelRepository;
    private final BidRepository bidRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 견적 생성
     * 사용자가 요청한 조건에 맞는 견적을 생성합니다.
     */
    @Transactional
    public void createQuote(QuoteCreateRequestDto request, User user) {
        PhoneModel phoneModel = phoneModelRepository.findById(request.getPhoneModelId())
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));

        PhoneOption colorOption = request.getColorOptionId() != null 
                ? phoneModel.findOptionById(request.getColorOptionId()) 
                : null;
        PhoneOption storageOption = request.getStorageOptionId() != null 
                ? phoneModel.findOptionById(request.getStorageOptionId()) 
                : null;

        Quote quote = request.toEntity(user, phoneModel, colorOption, storageOption);
        Quote savedQuote = quoteRepository.save(quote);
        
        eventPublisher.publishEvent(new QuoteCreatedEvent(this, savedQuote));
    }

    /**
     * 최신 진행중인 견적 목록 조회
     * 상태가 OPEN인 최신 견적 목록을 조회합니다.
     */
    public List<Quote> getLatestOpenQuotes(int limit) {
        return quoteRepository.findLatestQuotesByStatus(
                QuoteStatus.OPEN);
    }

    /**
     * 내 진행중인 견적 목록 조회
     * 사용자의 진행중인 견적을 페이징하여 조회합니다.
     */
    public Page<QuoteResponseDto> getMyOpenQuotes(User user, Pageable pageable) {
        Page<Quote> quotePage = quoteRepository.findByUserIdAndStatus(
                user.getId(), QuoteStatus.OPEN, pageable);
        return convertToPageDto(quotePage);
    }

    /**
     * 내 완료된 견적 목록 조회
     * 사용자의 완료된 견적(CLOSED, CONTRACTED)을 페이징하여 조회합니다.
     */
    public Page<QuoteResponseDto> getMyCompletedQuotes(User user, Pageable pageable) {
        List<QuoteStatus> completedStatuses = List.of(QuoteStatus.CLOSED, QuoteStatus.CONTRACTED);
        Page<Quote> quotePage = quoteRepository.findByUserIdAndStatusIn(
                user.getId(), completedStatuses, pageable);
        return convertToPageDto(quotePage);
    }

    /**
     * 전체 진행중인 견적 목록 조회
     * 모든 진행중인 견적을 조회합니다. 관리자 및 판매자 전용입니다.
     */
    public List<QuoteResponseDto> getAllOpenQuotes() {
        List<Quote> quotes = quoteRepository.findLatestQuotesByStatus(QuoteStatus.OPEN);
        return convertToListDto(quotes);
    }

    /**
     * 견적 상세 조회
     */
    public QuoteResponseDto getQuoteById(UUID quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));
        
        quote.validateNotDeleted();
        
        long bidCount = bidRepository.countByQuoteId(quoteId);
        Integer lowestPrice = bidRepository.findMinInstallmentPrincipalByQuoteId(
                quoteId, BidStatus.ACTIVE);
        
        return QuoteResponseDto.from(quote, bidCount, lowestPrice);
    }

    /**
     * 견적의 입찰 개수 조회
     */
    public long getBidCountByQuoteId(UUID quoteId) {
        return bidRepository.countByQuoteId(quoteId);
    }

    /**
     * 견적 종료
     * - 견적 소유자만 종료 가능
     * - OPEN 상태인 견적만 종료 가능
     */
    @Transactional
    public void closeQuote(UUID quoteId, User user) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));

        quote.validateNotDeleted();
        quote.validateOwnership(user.getId());
        
        quote.close();
    }

    // ========== Private 헬퍼 메서드 ==========

    /**
     * Page<Quote>를 Page<QuoteResponseDto>로 변환 (중복 제거)
     */
    private Page<QuoteResponseDto> convertToPageDto(Page<Quote> quotePage) {
        List<QuoteResponseDto> content = convertToListDto(quotePage.getContent());
        return new PageImpl<>(content, quotePage.getPageable(), quotePage.getTotalElements());
    }

    /**
     * Quote 리스트를 DTO 리스트로 변환
     * 입찰 개수와 최저가를 배치 쿼리로 조회하여 N+1 문제 방지
     * 
     * @param quotes 변환할 Quote 리스트
     * @return QuoteResponseDto 리스트
    */
    private List<QuoteResponseDto> convertToListDto(List<Quote> quotes) {
        if (quotes.isEmpty()) {
            return List.of();
        }

        List<UUID> quoteIds = quotes.stream()
                .map(Quote::getId)
                .collect(Collectors.toList());

        Map<UUID, Long> bidCountMap = bidRepository.countByQuoteIds(quoteIds).stream()
                .collect(Collectors.toMap(
                        BidRepository.BidCountDto::getQuoteId,
                        BidRepository.BidCountDto::getBidCount
                ));

        Map<UUID, Integer> lowestPriceMap = bidRepository
                .findMinInstallmentPrincipalByQuoteIds(quoteIds, BidStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        BidRepository.BidMinPriceDto::getQuoteId,
                        BidRepository.BidMinPriceDto::getMinPrice
                ));

        return quotes.stream()
                .map(quote -> QuoteResponseDto.from(
                        quote,
                        bidCountMap.getOrDefault(quote.getId(), 0L),
                        lowestPriceMap.get(quote.getId())
                ))
                .collect(Collectors.toList());
    }

}

