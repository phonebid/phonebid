package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.dto.response.QuoteResponseDto;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteService 테스트")
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private QuoteService quoteService;

    private User testUser;
    private Quote testQuote1;
    private Quote testQuote2;
    private QuoteCreateRequestDto quoteCreateRequestDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .role(Role.CONSUMER)
                .build();
        
        // UUID 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID", e);
        }

        // 테스트 Quote 생성
        testQuote1 = Quote.builder()
                .user(testUser)
                .model("iPhone 16")
                .storage("128GB")
                .carrier(Carrier.SKT)
                .color("블랙")
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .expiredAt(LocalDateTime.now().plusHours(24))
                .build();

        testQuote2 = Quote.builder()
                .user(testUser)
                .model("Galaxy S24")
                .storage("256GB")
                .carrier(Carrier.KT)
                .color("화이트")
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .expiredAt(LocalDateTime.now().plusHours(12))
                .build();

        // 테스트 요청 DTO 생성
        quoteCreateRequestDto = QuoteCreateRequestDto.builder()
                .model("iPhone 16")
                .storage("128GB")
                .color("블랙")
                .carrier(Carrier.SKT)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("getMyOpenQuotes - 성공: 사용자의 OPEN Quote 조회 성공")
    void getMyOpenQuotes_Success() {
        // given
        List<Quote> mockQuotes = Arrays.asList(testQuote1, testQuote2);
        when(quoteRepository.findByUserIdAndStatus(eq(testUser.getId()), eq(QuoteStatus.OPEN), eq(pageable)))
                .thenReturn(mockQuotes);

        // when
        List<QuoteResponseDto> result = quoteService.getMyOpenQuotes(testUser, pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getModel()).isEqualTo("iPhone 16");
        assertThat(result.get(1).getModel()).isEqualTo("Galaxy S24");
        verify(quoteRepository).findByUserIdAndStatus(testUser.getId(), QuoteStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("getMyOpenQuotes - 빈 목록: 해당 사용자의 Quote가 없는 경우")
    void getMyOpenQuotes_EmptyList() {
        // given
        when(quoteRepository.findByUserIdAndStatus(eq(testUser.getId()), eq(QuoteStatus.OPEN), eq(pageable)))
                .thenReturn(Collections.emptyList());

        // when
        List<QuoteResponseDto> result = quoteService.getMyOpenQuotes(testUser, pageable);

        // then
        assertThat(result).isEmpty();
        verify(quoteRepository).findByUserIdAndStatus(testUser.getId(), QuoteStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("getAllOpenQuotes - 성공: 모든 OPEN Quote 조회 성공")
    void getAllOpenQuotes_Success() {
        // given
        List<Quote> mockQuotes = Arrays.asList(testQuote1, testQuote2);
        when(quoteRepository.findLatestQuotesByStatus(eq(QuoteStatus.OPEN), eq(pageable)))
                .thenReturn(mockQuotes);

        // when
        List<QuoteResponseDto> result = quoteService.getAllOpenQuotes(pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getModel()).isEqualTo("iPhone 16");
        assertThat(result.get(1).getModel()).isEqualTo("Galaxy S24");
        verify(quoteRepository).findLatestQuotesByStatus(QuoteStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("getAllOpenQuotes - 빈 목록: OPEN Quote가 없는 경우")
    void getAllOpenQuotes_EmptyList() {
        // given
        when(quoteRepository.findLatestQuotesByStatus(eq(QuoteStatus.OPEN), eq(pageable)))
                .thenReturn(Collections.emptyList());

        // when
        List<QuoteResponseDto> result = quoteService.getAllOpenQuotes(pageable);

        // then
        assertThat(result).isEmpty();
        verify(quoteRepository).findLatestQuotesByStatus(QuoteStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("createQuote - 성공: 견적 생성 성공")
    void createQuote_Success() {
        // given
        when(quoteRepository.save(any(Quote.class))).thenReturn(testQuote1);

        // when
        quoteService.createQuote(quoteCreateRequestDto, testUser);

        // then
        verify(quoteRepository).save(any(Quote.class));
    }
}
