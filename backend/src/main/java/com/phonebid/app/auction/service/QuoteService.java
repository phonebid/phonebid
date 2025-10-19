package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private final QuoteRepository quoteRepository;

    @Transactional
    public void createQuote(QuoteCreateRequestDto quoteRequestDto, User user) {
        Quote quote = quoteRequestDto.toEntity(user);
        // validateQuote(quote);
        quoteRepository.save(quote);
    }

    public List<Quote> getLatestOpenQuotes(int limit) {
        return quoteRepository.findLatestQuotesByStatus(
                QuoteStatus.OPEN, PageRequest.of(0, limit));
    }

    private void validateQuote(Quote quote) {
        PurchaseMethod purchaseMethod = quote.getPurchaseMethod();
        if (purchaseMethod != null && purchaseMethod.requiresCurrentCarrier() && quote.getCurrentCarrier() == null) {
            throw new CustomException(AuctionErrorCode.QUOTE_CREATE_CURRENT_REQUIRED);
        }

        if (quote.getUser() == null) {
            throw new CustomException(AuctionErrorCode.QUOTE_CREATE_UNAUTHORIZED);
        }
    }
}

