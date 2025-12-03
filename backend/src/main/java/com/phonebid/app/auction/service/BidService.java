package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.*;
import com.phonebid.app.auction.dto.request.AdditionalServiceRequestDto;
import com.phonebid.app.auction.dto.request.BidCreateRequestDto;
import com.phonebid.app.auction.dto.response.BidListResponseDto;
import com.phonebid.app.auction.dto.response.BidResponseDto;
import com.phonebid.app.auction.repository.BidAdditionalServiceRepository;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.PricePlanRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final QuoteRepository quoteRepository;
    private final SellerRepository sellerRepository;
    private final PricePlanRepository pricePlanRepository;
    private final BidAdditionalServiceRepository bidAdditionalServiceRepository;

    /**
     * 입찰 생성
     * - 판매자당 견적당 1회만 입찰 가능
     * - 수정 불가
     */
    @Transactional
    public BidResponseDto createBid(BidCreateRequestDto requestDto, User user) {
        // 1. 판매자 조회 및 검증
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.SELLER_NOT_FOUND));
        
        if (!seller.canSell()) {
            throw new CustomException(AuctionErrorCode.SELLER_NOT_APPROVED);
        }

        // 2. 견적 조회 및 검증
        Quote quote = quoteRepository.findById(requestDto.getQuoteId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));
        
        if (!quote.canReceiveBids()) {
            throw new CustomException(AuctionErrorCode.QUOTE_EXPIRED);
        }

        // 3. 중복 입찰 체크
        if (bidRepository.existsByQuoteIdAndSellerId(quote.getId(), seller.getSellerId())) {
            throw new CustomException(AuctionErrorCode.DUPLICATE_BID);
        }

        // 4. 요금제 생성 및 저장
        PricePlan pricePlan = requestDto.toPricePlanEntity();
        pricePlanRepository.save(pricePlan);

        // 5. 입찰 생성
        Double ratingSnapshot = 4.5; // TODO: 실제 판매자 평점으로 교체
        Bid bid = requestDto.toBidEntity(quote, seller, pricePlan, ratingSnapshot);
        bidRepository.save(bid);

        // 6. 부가서비스 생성
        if (requestDto.getAdditionalServices() != null && !requestDto.getAdditionalServices().isEmpty()) {
            for (AdditionalServiceRequestDto serviceDto : requestDto.getAdditionalServices()) {
                BidAdditionalService additionalService = serviceDto.toEntity(bid);
                bidAdditionalServiceRepository.save(additionalService);
                bid.addAdditionalService(additionalService);
            }
        }

        log.info("입찰 생성 완료 - bidId: {}, quoteId: {}, sellerId: {}", 
                bid.getId(), quote.getId(), seller.getSellerId());

        return BidResponseDto.from(bid);
    }

    /**
     * 입찰 상세 조회
     */
    @Transactional(readOnly = true)
    public BidResponseDto getBidById(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.BID_NOT_FOUND));
        
        return BidResponseDto.from(bid);
    }

    /**
     * 특정 견적의 입찰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BidListResponseDto> getBidsByQuoteId(UUID quoteId) {
        // 견적 존재 확인
        if (!quoteRepository.existsById(quoteId)) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND);
        }

        List<Bid> bids = bidRepository.findActiveByQuoteId(quoteId, BidStatus.ACTIVE);
        return bids.stream()
                .map(BidListResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 판매자 본인의 입찰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BidListResponseDto> getMyBids(User user, int page, int size) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.SELLER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        List<Bid> bids = bidRepository.findBySellerId(seller.getSellerId(), pageable);
        
        return bids.stream()
                .map(BidListResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 판매자가 특정 견적에 이미 입찰했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasAlreadyBid(UUID quoteId, User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (seller == null) {
            return false;
        }
        
        return bidRepository.existsByQuoteIdAndSellerId(quoteId, seller.getSellerId());
    }

    /**
     * 특정 견적의 입찰 수 조회
     */
    @Transactional(readOnly = true)
    public long getBidCountByQuoteId(UUID quoteId) {
        return bidRepository.countByQuoteId(quoteId);
    }

    /**
     * 특정 견적의 최저 할부원금 조회
     */
    @Transactional(readOnly = true)
    public Integer getMinInstallmentPrincipal(UUID quoteId) {
        return bidRepository.findMinInstallmentPrincipalByQuoteId(quoteId, BidStatus.ACTIVE);
    }
}

