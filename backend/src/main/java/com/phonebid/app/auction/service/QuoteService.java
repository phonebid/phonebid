package com.phonebid.app.auction.service;

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

    @Transactional
    public void createQuote(QuoteCreateRequestDto quoteRequestDto, User user) {
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
        // validateQuote(quote);
        quoteRepository.save(quote);
    }

    public List<Quote> getLatestOpenQuotes(int limit) {
        return quoteRepository.findLatestQuotesByStatus(
                QuoteStatus.OPEN);
    }

    public List<QuoteResponseDto> getMyOpenQuotes(User user, Pageable pageable) {
        List<Quote> quotes = quoteRepository.findByUserIdAndStatus(
                user.getId(), QuoteStatus.OPEN, pageable);
        return quotes.stream()
                .map(QuoteResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<QuoteResponseDto> getAllOpenQuotes() {
        List<Quote> quotes = quoteRepository.findLatestQuotesByStatus(
                QuoteStatus.OPEN);
        return quotes.stream()
                .map(QuoteResponseDto::from)
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
        
        return QuoteResponseDto.from(quote, bidCount);
    }

    /**
     * 견적의 입찰 개수 조회
     */
    public long getBidCountByQuoteId(UUID quoteId) {
        return bidRepository.countByQuoteId(quoteId);
    }

}

