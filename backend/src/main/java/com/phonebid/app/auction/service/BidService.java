package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.*;
import com.phonebid.app.auction.dto.request.AdditionalServiceRequestDto;
import com.phonebid.app.auction.dto.request.BidCreateRequestDto;
import com.phonebid.app.auction.dto.request.BidUpdateRequestDto;
import com.phonebid.app.auction.dto.response.BidListResponseDto;
import com.phonebid.app.auction.dto.response.BidResponseDto;
import com.phonebid.app.auction.repository.BidAdditionalServiceRepository;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.PricePlanRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     * - 판매자는 동일 견적에 여러 번 입찰 가능
     */
    @Transactional
    public BidResponseDto createBid(BidCreateRequestDto requestDto, User user) {
        // 1. 판매자 조회 및 검증
        Seller seller = validateAndGetSeller(user);

        // 2. 견적 조회 및 검증
        Quote quote = validateAndGetQuote(requestDto.getQuoteId());

        // 3. 요금제 생성 및 저장
        PricePlan pricePlan = createAndSavePricePlan(requestDto);

        // 4. 입찰 생성
        Bid bid = createAndSaveBid(requestDto, quote, seller, pricePlan);

        // 5. 부가서비스 생성
        saveAdditionalServices(requestDto, bid);

        log.info("입찰 생성 완료 - bidId: {}, quoteId: {}, sellerId: {}", 
                bid.getId(), quote.getId(), seller.getSellerId());

        return BidResponseDto.from(bid);
    }

    // createBid 헬퍼메서드 시작
    // 판매자 조회 및 검증
    private Seller validateAndGetSeller(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.SELLER_NOT_FOUND));
        
        if (!seller.canSell()) {
            throw new CustomException(AuctionErrorCode.SELLER_NOT_APPROVED);
        }
        
        return seller;
    }

    // 견적 조회 및 검증
    private Quote validateAndGetQuote(UUID quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));
        
        if (!quote.canReceiveBids()) {
            throw new CustomException(AuctionErrorCode.QUOTE_EXPIRED);
        }
        
        return quote;
    }

    // 요금제 생성 및 저장
    private PricePlan createAndSavePricePlan(BidCreateRequestDto requestDto) {
        PricePlan pricePlan = requestDto.toPricePlanEntity();
        return pricePlanRepository.save(pricePlan);
    }

    // 입찰 생성 및 저장
    private Bid createAndSaveBid(BidCreateRequestDto requestDto, Quote quote, Seller seller, PricePlan pricePlan) {
        // TODO: seller.getRating()으로 교체 (Seller 엔티티에 getRating() 메서드 추가 필요)
        Double ratingSnapshot = 4.5;
        Bid bid = requestDto.toBidEntity(quote, seller, pricePlan, ratingSnapshot);
        return bidRepository.save(bid);
    }

    // 부가서비스 생성 및 저장
    private void saveAdditionalServices(BidCreateRequestDto requestDto, Bid bid) {
        if (requestDto.getAdditionalServices() != null && !requestDto.getAdditionalServices().isEmpty()) {
            for (AdditionalServiceRequestDto serviceDto : requestDto.getAdditionalServices()) {
                BidAdditionalService additionalService = serviceDto.toEntity(bid);
                bidAdditionalServiceRepository.save(additionalService);
                bid.addAdditionalService(additionalService);
            }
        }
    }

    // 헬퍼메서드 끝

    /**
     * 입찰 수정
     * - 본인의 입찰만 수정 가능
     * - ACTIVE 상태이고 견적이 아직 입찰을 받을 수 있는 상태여야 함
     */
    @Transactional
    public BidResponseDto updateBid(UUID bidId, BidUpdateRequestDto requestDto, User user) {
        // 1. 입찰 조회 및 검증
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.BID_NOT_FOUND));

        // 2. 판매자 조회 및 검증
        Seller seller = validateAndGetSeller(user);

        // 3. 본인의 입찰인지 확인
        if (!bid.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new CustomException(AuctionErrorCode.BID_NOT_ALLOWED);
        }

        // 4. 수정 가능한 상태인지 확인 (canModify 내부에서 ACTIVE 상태와 견적 상태 체크)
        if (!bid.canModify()) {
            throw new CustomException(AuctionErrorCode.BID_MODIFICATION_NOT_ALLOWED);
        }

        // 5. 요금제 업데이트 (요금제 정보가 제공된 경우)
        if (requestDto.getPricePlanName() != null || requestDto.getPricePlanPrice() != null) {
            PricePlan existingPricePlan = bid.getPricePlan();
            
            if (existingPricePlan != null) {
                // 기존 PricePlan이 있는 경우 필드 업데이트
                existingPricePlan.update(requestDto.getPricePlanName(), requestDto.getPricePlanPrice());
                pricePlanRepository.save(existingPricePlan);
            } else {
                // 기존 PricePlan이 없는 경우 새로 생성
                PricePlan newPricePlan = PricePlan.builder()
                        .carrier(bid.getCarrier())
                        .planName(requestDto.getPricePlanName())
                        .planPrice(requestDto.getPricePlanPrice())
                        .build();
                pricePlanRepository.save(newPricePlan);
                bid.updatePricePlan(newPricePlan);
            }
        }

        // 6. 부가서비스 업데이트 (부가서비스 목록이 제공된 경우)
        if (requestDto.getAdditionalServices() != null) {
            // 기존 부가서비스 삭제
            bidAdditionalServiceRepository.deleteAll(bid.getAdditionalServiceList());
            bid.getAdditionalServiceList().clear();

            // 새로운 부가서비스 추가
            List<BidAdditionalService> newServices = new ArrayList<>();
            for (AdditionalServiceRequestDto serviceDto : requestDto.getAdditionalServices()) {
                BidAdditionalService additionalService = serviceDto.toEntity(bid);
                bidAdditionalServiceRepository.save(additionalService);
                newServices.add(additionalService);
            }
            bid.replaceAdditionalServices(newServices);
        }

        // 7. 입찰 정보 업데이트
        BidUpdateCommand command = BidUpdateCommand.builder()
                .price(requestDto.getPrice())
                .deliveryDays(requestDto.getDeliveryDays())
                .additionalSubsidy(requestDto.getAdditionalSubsidy())
                .installmentPrincipal(requestDto.getInstallmentPrincipal())
                .contractMonths(requestDto.getContractMonths())
                .build();
        bid.updateBidDetails(command);

        log.info("입찰 수정 완료 - bidId: {}", bid.getId());

        return BidResponseDto.from(bid);
    }

    /**
     * 입찰 상세 조회
     * - 견적 소유자(소비자)만 조회 가능
     * - 판매자나 다른 사용자의 접근은 403 Forbidden으로 거부
     */
    @Transactional(readOnly = true)
    public BidResponseDto getBidById(UUID bidId, User user) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.BID_NOT_FOUND));
        
        // 견적 소유자(소비자)인지 확인
        Quote quote = bid.getQuote();
        if (!quote.getUser().getId().equals(user.getId())) {
            throw new CustomException(AuctionErrorCode.BID_ACCESS_DENIED);
        }
        
        return BidResponseDto.from(bid);
    }

    /**
     * 특정 견적의 입찰 목록 조회
     * - 견적 소유자(소비자): 모든 입찰 목록 조회 가능
     * - 판매자: 자신이 입찰한 입찰만 조회 가능
     * - 관리자: 모든 입찰 목록 조회 가능
     */
    @Transactional(readOnly = true)
    public List<BidListResponseDto> getBidsByQuoteId(UUID quoteId, User user) {
        // 1. 견적 조회 및 검증
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));

        // 2. 권한에 따라 입찰 목록 필터링
        List<Bid> bids;
        
        if (user.getRole() == Role.ADMIN) {
            // 관리자: 모든 입찰 조회
            bids = bidRepository.findActiveByQuoteId(quoteId, BidStatus.ACTIVE);
        } else if (user.getRole() == Role.CONSUMER) {
            // 소비자: 자신의 견적인지 확인 후 모든 입찰 조회
            if (!quote.getUser().getId().equals(user.getId())) {
                throw new CustomException(AuctionErrorCode.QUOTE_NOT_OWNED_BY_USER);
            }
            bids = bidRepository.findActiveByQuoteId(quoteId, BidStatus.ACTIVE);
        } else if (user.getRole() == Role.SELLER) {
            // 판매자: 자신이 입찰한 견적의 입찰 목록만 조회 가능
            Seller seller = sellerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CustomException(AuctionErrorCode.SELLER_NOT_FOUND));
            
            // 해당 견적에 입찰했는지 확인
            if (!bidRepository.existsByQuoteIdAndSellerId(quoteId, seller.getSellerId())) {
                throw new CustomException(AuctionErrorCode.BID_NOT_EXISTS_FOR_SELLER);
            }
            
            bids = bidRepository.findByQuoteIdAndSellerIdAndStatus(quoteId, seller.getSellerId(), BidStatus.ACTIVE);
        } else {
            throw new CustomException(AuctionErrorCode.BID_NOT_ALLOWED);
        }

        return bids.stream()
                .map(BidListResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 판매자 본인의 입찰 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<BidListResponseDto> getMyBids(User user, int page, int size) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.SELLER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<Bid> bidPage = bidRepository.findBySellerId(seller.getSellerId(), pageable);
        
        return bidPage.map(BidListResponseDto::from);
    }

    /**
     * 판매자가 특정 견적에 입찰한 이력이 있는지 확인
     * (정보 제공용, 입찰 제한은 하지 않음)
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

