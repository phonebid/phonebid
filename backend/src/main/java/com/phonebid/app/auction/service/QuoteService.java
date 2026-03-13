package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    private final BidService bidService;

    /**
     * 견적 생성
     * 사용자가 요청한 조건에 맞는 견적을 생성합니다.
     */
    @Transactional
    public void createQuote(QuoteCreateRequestDto quoteRequestDto, User user) {
        // 본인인증 확인
        if (!Boolean.TRUE.equals(user.getIsIdentityVerified())) {
            throw new CustomException(AuctionErrorCode.IDENTITY_VERIFICATION_REQUIRED);
        }

        // 통신사 관련 검증
        validateCarrierRules(quoteRequestDto, user);

        PhoneModel phoneModel = phoneModelRepository.findById(quoteRequestDto.getPhoneModelId())
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));

        PhoneOption colorOption = null;
        if (quoteRequestDto.getColorOptionId() != null) {
            colorOption = phoneModel.getOptions().stream()
                .filter(phoneOption -> phoneOption.getId().equals(quoteRequestDto.getColorOptionId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND));
        }

        PhoneOption storageOption = null;
        if (quoteRequestDto.getStorageOptionId() != null) {
            storageOption = phoneModel.getOptions().stream()
                .filter(phoneOption -> phoneOption.getId().equals(quoteRequestDto.getStorageOptionId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND));
        }

        Quote quote = quoteRequestDto.toEntity(user, phoneModel, colorOption, storageOption);
        quoteRepository.save(quote);
    }

    private void validateCarrierRules(QuoteCreateRequestDto dto, User user) {
        PurchaseMethod purchaseMethod = dto.getPurchaseMethod();
        if (purchaseMethod == null) return;

        Carrier userCarrier = user.getCarrier();

        if (purchaseMethod == PurchaseMethod.DEVICE_CHANGE) {
            // 알뜰폰 사용자는 기기변경 불가
            if (userCarrier != null && userCarrier.isMVNO()) {
                throw new CustomException(AuctionErrorCode.MVNO_DEVICE_CHANGE_NOT_ALLOWED);
            }
            // 기기변경 시 현재 통신사와 동일해야 함
            Carrier requestedCarrier = dto.getCurrentCarrier();
            if (userCarrier != null && requestedCarrier != null && requestedCarrier != userCarrier) {
                throw new CustomException(AuctionErrorCode.INVALID_DEVICE_CHANGE_CARRIER);
            }
        }

        if (purchaseMethod == PurchaseMethod.NUMBER_TRANSFER) {
            // 번호이동 시 대상 통신사는 현재 통신사 제외한 주요 통신사만
            Carrier targetCarrier = dto.getCarrier();
            if (targetCarrier != null) {
                if (!targetCarrier.isMajor()) {
                    throw new CustomException(AuctionErrorCode.INVALID_NUMBER_TRANSFER_CARRIER);
                }
                if (userCarrier != null && targetCarrier == userCarrier) {
                    throw new CustomException(AuctionErrorCode.INVALID_NUMBER_TRANSFER_CARRIER);
                }
            }
        }
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
        
        List<QuoteResponseDto> content = quotePage.getContent().stream()
                .map(quote -> {
                    Long bidCount = bidRepository.countByQuoteId(quote.getId());
                    Integer lowestPrice = bidService.getMinInstallmentPrincipal(quote.getId());
                    return QuoteResponseDto.from(quote, bidCount, lowestPrice);
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, quotePage.getTotalElements());
    }

    /**
     * 내 완료된 견적 목록 조회
     * 사용자의 완료된 견적(CLOSED, CONTRACTED)을 페이징하여 조회합니다.
     */
    public Page<QuoteResponseDto> getMyCompletedQuotes(User user, Pageable pageable) {
        List<QuoteStatus> completedStatuses = List.of(QuoteStatus.CLOSED, QuoteStatus.CONTRACTED);
        Page<Quote> quotePage = quoteRepository.findByUserIdAndStatusIn(
                user.getId(), completedStatuses, pageable);
        
        List<QuoteResponseDto> content = quotePage.getContent().stream()
                .map(quote -> {
                    Long bidCount = bidRepository.countByQuoteId(quote.getId());
                    Integer lowestPrice = bidService.getMinInstallmentPrincipal(quote.getId());
                    return QuoteResponseDto.from(quote, bidCount, lowestPrice);
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, quotePage.getTotalElements());
    }

    /**
     * 전체 진행중인 견적 목록 조회
     * 모든 진행중인 견적을 조회합니다. 관리자 및 판매자 전용입니다.
     */
    public List<QuoteResponseDto> getAllOpenQuotes() {
        List<Quote> quotes = quoteRepository.findLatestQuotesByStatus(
                QuoteStatus.OPEN);
        return quotes.stream()
                .map(quote -> {
                    Long bidCount = bidRepository.countByQuoteId(quote.getId());
                    Integer lowestPrice = bidService.getMinInstallmentPrincipal(quote.getId());
                    return QuoteResponseDto.from(quote, bidCount, lowestPrice);
                })
                .collect(Collectors.toList());
    }

    /**
     * 견적 상세 조회
     */
    public QuoteResponseDto getQuoteById(UUID quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));
        
        // 삭제된 견적인지 확인
        if (quote.getIsDelete() != null && quote.getIsDelete()) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND);
        }
        
        // 입찰 개수 조회
        long bidCount = bidRepository.countByQuoteId(quoteId);
        
        // 최저 할부원금 조회
        Integer lowestPrice = bidService.getMinInstallmentPrincipal(quoteId);
        
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

        // 삭제된 견적인지 확인
        if (quote.getIsDelete() != null && quote.getIsDelete()) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND);
        }

        // 견적 소유자 확인
        if (!quote.getUser().getId().equals(user.getId())) {
            throw new CustomException(AuctionErrorCode.QUOTE_NOT_OWNED_BY_USER);
        }

        // 이미 종료되었거나 계약 완료된 견적인지 확인
        if (quote.getStatus() == QuoteStatus.CLOSED) {
            throw new CustomException(AuctionErrorCode.QUOTE_ALREADY_CLOSED);
        }
        if (quote.getStatus() == QuoteStatus.CONTRACTED) {
            throw new CustomException(AuctionErrorCode.INVALID_QUOTE_STATUS);
        }

        // 견적 종료
        quote.close();
        quoteRepository.save(quote);
    }

}

