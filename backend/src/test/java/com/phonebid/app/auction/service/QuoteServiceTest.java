package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.dto.response.QuoteResponseDto;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.repository.PhoneModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Mock
    private PhoneModelRepository phoneModelRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private BidService bidService;

    @InjectMocks
    private QuoteService quoteService;

    private User testUser;
    private Quote testQuote1;
    private Quote testQuote2;
    private QuoteCreateRequestDto quoteCreateRequestDto;
    private PhoneModel phoneModel;
    private PhoneOption storageOption;
    private PhoneOption colorOption;
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

        // PhoneModel 및 옵션 생성
        phoneModel = PhoneModel.builder()
                .brand(Brand.APPLE)
                .model("iPhone 16")
                .build();
        try {
            java.lang.reflect.Field idField = PhoneModel.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(phoneModel, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID for PhoneModel", e);
        }
        storageOption = PhoneOption.builder()
                .model(phoneModel)
                .optionType(PhoneOption.OptionType.STORAGE)
                .optionValue("128")
                .displayLabel("128GB")
                .build();
        colorOption = PhoneOption.builder()
                .model(phoneModel)
                .optionType(PhoneOption.OptionType.COLOR)
                .optionValue("BLACK")
                .displayLabel("블랙")
                .build();
        try {
            java.lang.reflect.Field idField = PhoneOption.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(storageOption, UUID.randomUUID());
            idField.set(colorOption, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID for PhoneOption", e);
        }

        // PhoneModel 옵션 리스트 주입 (서비스에서 phoneModel.getOptions() 검색함)
        try {
            java.lang.reflect.Field optionsField = PhoneModel.class.getDeclaredField("options");
            optionsField.setAccessible(true);
            optionsField.set(phoneModel, java.util.Arrays.asList(storageOption, colorOption));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set options for PhoneModel", e);
        }

        // 테스트 Quote 생성 (연관관계 기반)
        testQuote1 = Quote.builder()
                .user(testUser)
                .phoneModel(phoneModel)
                .storage(storageOption)
                .carrier(Carrier.SKT)
                .color(colorOption)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .expiredAt(LocalDateTime.now().plusHours(24))
                .build();
        try {
            java.lang.reflect.Field idField = Quote.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testQuote1, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID for Quote", e);
        }

        PhoneModel phoneModel2 = PhoneModel.builder()
                .brand(Brand.SAMSUNG)
                .model("Galaxy S24")
                .build();
        try {
            java.lang.reflect.Field idField = PhoneModel.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(phoneModel2, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID for PhoneModel", e);
        }
        PhoneOption storageOption2 = PhoneOption.builder()
                .model(phoneModel2)
                .optionType(PhoneOption.OptionType.STORAGE)
                .optionValue("256")
                .displayLabel("256GB")
                .build();
        PhoneOption colorOption2 = PhoneOption.builder()
                .model(phoneModel2)
                .optionType(PhoneOption.OptionType.COLOR)
                .optionValue("WHITE")
                .displayLabel("화이트")
                .build();
        try {
            java.lang.reflect.Field idField = PhoneOption.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(storageOption2, UUID.randomUUID());
            idField.set(colorOption2, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID for PhoneOption", e);
        }
        testQuote2 = Quote.builder()
                .user(testUser)
                .phoneModel(phoneModel2)
                .storage(storageOption2)
                .carrier(Carrier.KT)
                .color(colorOption2)
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .expiredAt(LocalDateTime.now().plusHours(12))
                .build();
        try {
            java.lang.reflect.Field idField = Quote.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testQuote2, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID for Quote", e);
        }

        // 테스트 요청 DTO 생성 (ID 기반)
        quoteCreateRequestDto = QuoteCreateRequestDto.builder()
                .phoneModelId(getUuid(phoneModel))
                .storageOptionId(getUuid(storageOption))
                .colorOptionId(getUuid(colorOption))
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
        Page<Quote> mockPage = new PageImpl<>(mockQuotes, pageable, mockQuotes.size());
        when(quoteRepository.findByUserIdAndStatus(eq(testUser.getId()), eq(QuoteStatus.OPEN), eq(pageable)))
                .thenReturn(mockPage);
        when(bidRepository.countByQuoteId(eq(testQuote1.getId()))).thenReturn(0L);
        when(bidRepository.countByQuoteId(eq(testQuote2.getId()))).thenReturn(0L);
        when(bidService.getMinInstallmentPrincipal(eq(testQuote1.getId()))).thenReturn(null);
        when(bidService.getMinInstallmentPrincipal(eq(testQuote2.getId()))).thenReturn(null);

        // when
        Page<QuoteResponseDto> result = quoteService.getMyOpenQuotes(testUser, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getPhoneModel().getModel()).isEqualTo("iPhone 16");
        assertThat(result.getContent().get(1).getPhoneModel().getModel()).isEqualTo("Galaxy S24");
        verify(quoteRepository).findByUserIdAndStatus(testUser.getId(), QuoteStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("getMyOpenQuotes - 빈 목록: 해당 사용자의 Quote가 없는 경우")
    void getMyOpenQuotes_EmptyList() {
        // given
        Page<Quote> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(quoteRepository.findByUserIdAndStatus(eq(testUser.getId()), eq(QuoteStatus.OPEN), eq(pageable)))
                .thenReturn(emptyPage);

        // when
        Page<QuoteResponseDto> result = quoteService.getMyOpenQuotes(testUser, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        verify(quoteRepository).findByUserIdAndStatus(testUser.getId(), QuoteStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("getAllOpenQuotes - 성공: 모든 OPEN Quote 조회 성공")
    void getAllOpenQuotes_Success() {
        // given
        List<Quote> mockQuotes = Arrays.asList(testQuote1, testQuote2);
        when(quoteRepository.findLatestQuotesByStatus(eq(QuoteStatus.OPEN)))
                .thenReturn(mockQuotes);
        when(bidRepository.countByQuoteId(eq(testQuote1.getId()))).thenReturn(0L);
        when(bidRepository.countByQuoteId(eq(testQuote2.getId()))).thenReturn(0L);
        when(bidService.getMinInstallmentPrincipal(eq(testQuote1.getId()))).thenReturn(null);
        when(bidService.getMinInstallmentPrincipal(eq(testQuote2.getId()))).thenReturn(null);

        // when
        List<QuoteResponseDto> result = quoteService.getAllOpenQuotes();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPhoneModel().getModel()).isEqualTo("iPhone 16");
        assertThat(result.get(1).getPhoneModel().getModel()).isEqualTo("Galaxy S24");
        verify(quoteRepository).findLatestQuotesByStatus(QuoteStatus.OPEN);
    }

    @Test
    @DisplayName("getAllOpenQuotes - 빈 목록: OPEN Quote가 없는 경우")
    void getAllOpenQuotes_EmptyList() {
        // given
        when(quoteRepository.findLatestQuotesByStatus(eq(QuoteStatus.OPEN)))
                .thenReturn(Collections.emptyList());

        // when
        List<QuoteResponseDto> result = quoteService.getAllOpenQuotes();

        // then
        assertThat(result).isEmpty();
        verify(quoteRepository).findLatestQuotesByStatus(QuoteStatus.OPEN);
    }

    @Test
    @DisplayName("createQuote - 성공: 견적 생성 성공")
    void createQuote_Success() {
        // given
        when(phoneModelRepository.findById(eq(getUuid(phoneModel)))).thenReturn(java.util.Optional.of(phoneModel));
        when(quoteRepository.save(any(Quote.class))).thenReturn(testQuote1);

        // when
        quoteService.createQuote(quoteCreateRequestDto, testUser);

        // then
        verify(quoteRepository).save(any(Quote.class));
    }

    @Test
    @DisplayName("createQuote - 성공: 색상/용량 옵션 없이 견적 생성 성공")
    void createQuote_Success_WithoutOptions() {
        // given
        QuoteCreateRequestDto requestWithoutOptions = QuoteCreateRequestDto.builder()
                .phoneModelId(getUuid(phoneModel))
                .storageOptionId(null)
                .colorOptionId(null)
                .carrier(Carrier.SKT)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build();
        
        when(phoneModelRepository.findById(eq(getUuid(phoneModel)))).thenReturn(java.util.Optional.of(phoneModel));
        when(quoteRepository.save(any(Quote.class))).thenReturn(testQuote1);

        // when
        quoteService.createQuote(requestWithoutOptions, testUser);

        // then
        verify(quoteRepository).save(any(Quote.class));
    }

    private java.util.UUID getUuid(Object entity) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (java.util.UUID) idField.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get UUID via reflection", e);
        }
    }
}
