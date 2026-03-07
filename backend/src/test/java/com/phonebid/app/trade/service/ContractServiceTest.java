package com.phonebid.app.trade.service;

import com.phonebid.app.auction.domain.*;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.errorcode.TradeErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.notification.event.BidSelectedEvent;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.trade.domain.Contract;
import com.phonebid.app.trade.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractService 비관적 락 테스트")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ContractService contractService;

    private User testUser;
    private Seller testSeller;
    private PhoneModel testPhoneModel;
    private Quote testQuote;
    private Bid testBid;
    private PricePlan testPricePlan;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = createTestUser("testuser@test.com", "테스트 사용자", "010-1234-5678", Role.CONSUMER);

        // 테스트용 User 생성 (Seller를 위한 User)
        User sellerUser = createTestUser("seller@test.com", "테스트 판매자", "010-9876-5432", Role.SELLER);

        // 테스트용 Seller 생성
        testSeller = Seller.builder()
                .user(sellerUser)
                .businessNumber("123-45-67890")
                .storeName("테스트 상점")
                .representativeName("대표자명")
                .isAgent(false)
                .build();

        // 테스트용 PhoneModel 생성
        testPhoneModel = PhoneModel.builder()
                .brand(Brand.APPLE)
                .model("iPhone 16 Pro")
                .modelNumber("A3101")
                .releasedPrice(1500000)
                .build();
        setEntityId(testPhoneModel, UUID.randomUUID());

        // 테스트용 PricePlan 생성
        testPricePlan = PricePlan.builder()
                .carrier(Carrier.SKT)
                .planName("5G 프리미엄")
                .planPrice(60000)
                .build();
        setEntityId(testPricePlan, UUID.randomUUID());

        // 테스트용 Quote 생성 (OPEN 상태) - spy로 감싸서 상태 변경 후에도 canSelectBid() 동작 제어
        Quote rawQuote = Quote.builder()
                .user(testUser)
                .phoneModel(testPhoneModel)
                .carrier(Carrier.SKT)
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.NUMBER_TRANSFER)
                .currentCarrier(Carrier.KT) // 번호이동 시 기존 통신사 필수
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build();
        setEntityId(rawQuote, UUID.randomUUID());
        testQuote = spy(rawQuote);
        lenient().doReturn(true).when(testQuote).canSelectBid();

        // 테스트용 Bid 생성 (ACTIVE 상태) - spy로 감싸서 상태 변경 후에도 isActive() 동작 제어
        Bid rawBid = Bid.builder()
                .quote(testQuote)
                .seller(testSeller)
                .pricePlan(testPricePlan)
                .price(800000)
                .deliveryDays(3)
                .purchaseMethod(PurchaseMethod.NUMBER_TRANSFER)
                .carrier(Carrier.SKT)
                .currentCarrier(Carrier.KT) // 번호이동 시 기존 통신사 필수
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .installmentPrincipal(700000)
                .contractMonths(24)
                .build();
        setEntityId(rawBid, UUID.randomUUID());
        testBid = spy(rawBid);
        lenient().doReturn(true).when(testBid).isActive();
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드
     * Reflection을 사용하여 ID를 설정
     */
    private User createTestUser(String username, String name, String phone, Role role) {
        User user = User.builder()
                .username(username)
                .password("password123")
                .email(username) // username을 email로 사용
                .name(name)
                .nickname(name)
                .phone(phone)
                .role(role)
                .build();
        
        // Reflection을 사용하여 ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set User ID for test", e);
        }
        
        return user;
    }

    /**
     * 엔티티에 ID를 설정하는 헬퍼 메서드 (범용)
     */
    private <T> void setEntityId(T entity, UUID id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException e) {
            // 상위 클래스에서 찾기 시도
            try {
                java.lang.reflect.Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set entity ID for test", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID for test", e);
        }
    }

    @Test
    @DisplayName("정상적인 계약 생성 - 비관적 락 적용 성공")
    void createContract_Success() {
        // Given
        UUID quoteId = testQuote.getId(); // testQuote의 실제 ID 사용
        UUID bidId = testBid.getId(); // testBid의 실제 ID 사용

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(bidRepository.findByIdWithLock(bidId)).thenReturn(Optional.of(testBid));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract contract = invocation.getArgument(0);
            return contract;
        });

        // When
        Contract result = contractService.createContract(quoteId, bidId, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuote()).isEqualTo(testQuote);
        assertThat(result.getSelectedBid()).isEqualTo(testBid);

        // 비관적 락이 적용된 메서드가 호출되었는지 확인
        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(bidRepository, times(1)).findByIdWithLock(bidId);
        verify(contractRepository, times(1)).existsByQuoteId(quoteId);
        verify(contractRepository, times(1)).save(any(Contract.class));
        verify(eventPublisher, times(1)).publishEvent(any(BidSelectedEvent.class));
    }

    @Test
    @DisplayName("견적 조회 실패 - Quote가 존재하지 않음")
    void createContract_QuoteNotFound() {
        // Given
        UUID quoteId = UUID.randomUUID();
        UUID bidId = UUID.randomUUID();

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(quoteId, bidId, testUser))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(AuctionErrorCode.QUOTE_NOT_FOUND.getMessage());

        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(bidRepository, never()).findByIdWithLock(any());
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("권한 확인 실패 - 견적 소유자가 아님")
    void createContract_NotQuoteOwner() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = testBid.getId();
        User anotherUser = createTestUser("another@test.com", "다른 사용자", "010-0000-0000", Role.CONSUMER);

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(quoteId, bidId, anotherUser))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(AuctionErrorCode.QUOTE_NOT_OWNED_BY_USER.getMessage());

        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(contractRepository, never()).existsByQuoteId(any());
        verify(bidRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("중복 계약 방지 - 이미 계약이 존재함")
    void createContract_ContractAlreadyExists() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = testBid.getId();

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(true); // 이미 계약 존재

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(quoteId, bidId, testUser))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(TradeErrorCode.CONTRACT_ALREADY_EXISTS.getMessage());

        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(contractRepository, times(1)).existsByQuoteId(quoteId);
        verify(bidRepository, never()).findByIdWithLock(any());
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("견적 상태 확인 실패 - Quote가 OPEN 상태가 아님")
    void createContract_QuoteNotOpen() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = testBid.getId();

        // Quote를 CLOSED 상태로 변경
        testQuote.close();

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(quoteId, bidId, testUser))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(AuctionErrorCode.INVALID_QUOTE_STATUS.getMessage());

        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(contractRepository, times(1)).existsByQuoteId(quoteId);
        verify(bidRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("입찰 조회 실패 - Bid가 존재하지 않음")
    void createContract_BidNotFound() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = UUID.randomUUID(); // 존재하지 않는 Bid ID

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(bidRepository.findByIdWithLock(bidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(quoteId, bidId, testUser))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(AuctionErrorCode.BID_NOT_FOUND.getMessage());

        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(bidRepository, times(1)).findByIdWithLock(bidId);
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("입찰 검증 실패 - 입찰이 해당 견적에 속하지 않음")
    void createContract_InvalidBidForQuote() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = UUID.randomUUID();

        // 다른 견적에 속한 입찰 생성
        Quote anotherQuote = Quote.builder()
                .user(testUser)
                .phoneModel(testPhoneModel)
                .carrier(Carrier.KT)
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .currentCarrier(Carrier.KT) // 기기변경 시 기존 통신사 필수
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .build();
        setEntityId(anotherQuote, UUID.randomUUID());

        Bid anotherBid = Bid.builder()
                .quote(anotherQuote) // 다른 견적
                .seller(testSeller)
                .pricePlan(testPricePlan)
                .price(900000)
                .deliveryDays(5)
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .carrier(Carrier.KT)
                .currentCarrier(Carrier.KT) // 기기변경 시 기존 통신사 필수
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .installmentPrincipal(800000)
                .contractMonths(24)
                .build();
        setEntityId(anotherBid, UUID.randomUUID());

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(bidRepository.findByIdWithLock(bidId)).thenReturn(Optional.of(anotherBid));

        // When & Then
        assertThatThrownBy(() -> contractService.createContract(quoteId, bidId, testUser))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(TradeErrorCode.INVALID_BID_FOR_QUOTE.getMessage());

        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(bidRepository, times(1)).findByIdWithLock(bidId);
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("동시성 테스트 시뮬레이션 - 락이 순차적으로 획득되는지 확인")
    void createContract_ConcurrencySimulation() throws InterruptedException {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId1 = testBid.getId();
        UUID bidId2 = UUID.randomUUID();

        // 각 스레드가 별도 Bid spy를 받도록 두 번째 Bid 생성
        Bid rawBid2 = Bid.builder()
                .quote(testQuote)
                .seller(testSeller)
                .pricePlan(testPricePlan)
                .price(850000)
                .deliveryDays(3)
                .purchaseMethod(PurchaseMethod.NUMBER_TRANSFER)
                .carrier(Carrier.SKT)
                .currentCarrier(Carrier.KT)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .installmentPrincipal(700000)
                .contractMonths(24)
                .build();
        setEntityId(rawBid2, bidId2);
        Bid testBid2 = spy(rawBid2);
        lenient().doReturn(true).when(testBid2).isActive();

        AtomicInteger lockAcquireCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 첫 번째 호출은 성공, 두 번째 호출은 이미 계약 존재로 실패
        when(quoteRepository.findByIdWithLock(quoteId)).thenAnswer(invocation -> {
            lockAcquireCount.incrementAndGet();
            Thread.sleep(50);
            return Optional.of(testQuote);
        });

        when(contractRepository.existsByQuoteId(quoteId))
                .thenReturn(false)  // 첫 번째 호출
                .thenReturn(true);  // 두 번째 호출 (이미 계약 존재)

        lenient().when(bidRepository.findByIdWithLock(bidId1)).thenReturn(Optional.of(testBid));
        lenient().when(bidRepository.findByIdWithLock(bidId2)).thenReturn(Optional.of(testBid2));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // When: 2개의 스레드가 동시에 계약 생성 시도
        executor.submit(() -> {
            try {
                startLatch.await();
                contractService.createContract(quoteId, bidId1, testUser);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                contractService.createContract(quoteId, bidId2, testUser);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await();

        // Then
        assertThat(lockAcquireCount.get()).isEqualTo(2);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        verify(quoteRepository, times(2)).findByIdWithLock(quoteId);
        verify(contractRepository, times(1)).save(any(Contract.class));

        executor.shutdown();
    }

    @Test
    @DisplayName("비관적 락 메서드 사용 확인 - findByIdWithLock 호출")
    void createContract_VerifyPessimisticLockMethodsAreCalled() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = testBid.getId();

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(bidRepository.findByIdWithLock(bidId)).thenReturn(Optional.of(testBid));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        contractService.createContract(quoteId, bidId, testUser);

        // Then: 일반 findById가 아닌 findByIdWithLock이 호출되었는지 확인
        verify(quoteRepository, times(1)).findByIdWithLock(quoteId);
        verify(quoteRepository, never()).findById(any()); // 일반 조회는 호출되지 않음

        verify(bidRepository, times(1)).findByIdWithLock(bidId);
        verify(bidRepository, never()).findById(any()); // 일반 조회는 호출되지 않음
    }

    @Test
    @DisplayName("이벤트 발행 확인 - BidSelectedEvent 발행")
    void createContract_PublishesBidSelectedEvent() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = testBid.getId();

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(bidRepository.findByIdWithLock(bidId)).thenReturn(Optional.of(testBid));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        contractService.createContract(quoteId, bidId, testUser);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(BidSelectedEvent.class));
    }

    @Test
    @DisplayName("계약 생성 후 상태 변경 확인 - Quote와 Bid 상태 업데이트")
    void createContract_UpdatesQuoteAndBidStatus() {
        // Given
        UUID quoteId = testQuote.getId();
        UUID bidId = testBid.getId();

        when(quoteRepository.findByIdWithLock(quoteId)).thenReturn(Optional.of(testQuote));
        when(contractRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(bidRepository.findByIdWithLock(bidId)).thenReturn(Optional.of(testBid));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        contractService.createContract(quoteId, bidId, testUser);

        // Then
        assertThat(testQuote.getStatus()).isEqualTo(QuoteStatus.CONTRACTED);
        assertThat(testBid.getStatus()).isEqualTo(BidStatus.SELECTED);

        verify(bidRepository, times(1)).save(testBid);
        verify(quoteRepository, times(1)).save(testQuote);
    }
}
