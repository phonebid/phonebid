package com.phonebid.app.auction.domain;

import com.phonebid.app.common.domain.Address;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class BidTest {

    private User consumer;
    private User sellerUser;
    private Seller seller;
    private Quote quote;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        consumer = User.builder()
                .email("consumer@test.com")
                .name("소비자")
                .role(Role.CONSUMER)
                .provider(Provider.KAKAO)
                .providerId("consumer123")
                .build();

        sellerUser = User.builder()
                .email("seller@test.com")
                .name("판매자")
                .role(Role.SELLER)
                .provider(Provider.NAVER)
                .providerId("seller123")
                .build();

        seller = Seller.builder()
                .user(sellerUser)
                .businessNumber("123-45-67890")
                .storeName("테스트 스토어")
                .storeAddress(createTestAddress())
                .build();
        seller.approve(); // 승인된 판매자로 설정

        quote = Quote.builder()
                .user(consumer)
                .model("iPhone 16")
                .storage("128GB")
                .carrier(Carrier.SKT)
                .color("블랙")
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.ANY)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.ANY)
                .build();
    }

    @Test
    @DisplayName("기본 입찰을 정상적으로 생성할 수 있다")
    void createBasicBid() {
        // given
        PricePlan pricePlan = createTestPricePlan();

        // when
        Bid bid = Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1200000)
                .deliveryDays(3)
                .ratingSnapshot(4.5)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .carrier(Carrier.SKT)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .additionalSubsidy(50000)
                .installmentPrincipal(1000000)
                .additionalServices("보험, 액세서리")
                .pricePlan(pricePlan)
                .contractMonths(24)
                .build();

        // then
        assertThat(bid.getQuote()).isEqualTo(quote);
        assertThat(bid.getSeller()).isEqualTo(seller);
        assertThat(bid.getPrice()).isEqualTo(1200000);
        assertThat(bid.getDeliveryDays()).isEqualTo(3);
        assertThat(bid.getPurchaseMethod()).isEqualTo(PurchaseMethod.NEW_SUBSCRIPTION);
        assertThat(bid.getActivationMethod()).isEqualTo(ActivationMethod.SELECTIVE_SUBSIDY);
        assertThat(bid.getContractMonths()).isEqualTo(24);
        assertThat(bid.hasPricePlan()).isTrue();
        assertThat(bid.hasContract()).isTrue();
    }

    @Test
    @DisplayName("번호이동 입찰을 생성할 수 있다")
    void createNumberTransferBid() {
        // when
        Bid bid = Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1100000)
                .deliveryDays(2)
                .ratingSnapshot(4.8)
                .purchaseMethod(PurchaseMethod.NUMBER_TRANSFER)
                .carrier(Carrier.SKT) // 이동할 통신사
                .currentCarrier(Carrier.KT) // 기존 통신사
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .additionalSubsidy(30000)
                .installmentPrincipal(900000)
                .additionalServices("케이스")
                .pricePlan(createTestPricePlan())
                .contractMonths(12)
                .build();

        // then
        assertThat(bid.getPurchaseMethod()).isEqualTo(PurchaseMethod.NUMBER_TRANSFER);
        assertThat(bid.getCurrentCarrier()).isEqualTo(Carrier.KT);
        assertThat(bid.isNumberTransfer()).isTrue();
        assertThat(bid.requiresCurrentCarrier()).isTrue();
    }

    @Test
    @DisplayName("기기변경 입찰을 생성할 수 있다")
    void createDeviceChangeBid() {
        // when
        Bid bid = Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1300000)
                .deliveryDays(1)
                .ratingSnapshot(4.2)
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .carrier(Carrier.SKT) // 동일 통신사
                .currentCarrier(Carrier.SKT) // 동일 통신사
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .additionalSubsidy(100000)
                .installmentPrincipal(1200000)
                .additionalServices("무선충전기, 보호필름")
                .pricePlan(createTestPricePlan())
                .contractMonths(36)
                .build();

        // then
        assertThat(bid.getPurchaseMethod()).isEqualTo(PurchaseMethod.DEVICE_CHANGE);
        assertThat(bid.getCurrentCarrier()).isEqualTo(Carrier.SKT);
        assertThat(bid.requiresCurrentCarrier()).isTrue();
    }

    @Test
    @DisplayName("총 비용을 올바르게 계산한다")
    void calculateTotalCost() {
        // given
        PricePlan pricePlan = PricePlan.builder()
                .planName("5G 프리미엄")
                .planPrice(80000)
                .build();

        Bid bid = Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1000000) // 입찰가
                .deliveryDays(3)
                .ratingSnapshot(4.5)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .carrier(Carrier.SKT)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .additionalSubsidy(50000) // 추가지원금
                .installmentPrincipal(900000)
                .additionalServices("보험")
                .pricePlan(pricePlan) // 80,000원 * 24개월 = 1,920,000원
                .contractMonths(24)
                .build();

        // when
        Integer totalCost = bid.getTotalCost();
        Integer monthlyAverage = bid.getMonthlyAverageCost();

        // then
        // 1,000,000 (입찰가) + 50,000 (추가지원금) + 1,920,000 (요금제) = 2,970,000
        assertThat(totalCost).isEqualTo(2970000);
        assertThat(monthlyAverage).isEqualTo(123750); // 2,970,000 / 24
    }

    @Test
    @DisplayName("확장된 입찰 요약 정보를 올바르게 생성한다")
    void getBidSummaryWithExtendedInfo() {
        // given
        PricePlan pricePlan = createTestPricePlan();
        
        Bid bid = Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1200000)
                .deliveryDays(3)
                .ratingSnapshot(4.5)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .carrier(Carrier.SKT)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .additionalSubsidy(50000)
                .installmentPrincipal(1000000)
                .additionalServices("보험, 액세서리")
                .pricePlan(pricePlan)
                .contractMonths(24)
                .build();

        // when
        String summary = bid.getBidSummary();

        // then
        assertThat(summary).contains("입찰가: 1,200,000원");
        assertThat(summary).contains("배송예정: 3일");
        assertThat(summary).contains("요금제: 5G 스탠다드 (75,000원)");
        assertThat(summary).contains("약정: 24개월");
    }


    @Test
    @DisplayName("구매방법이 ANY이면 입찰 생성에 실패한다")
    void createBid_WithAnyPurchaseMethod_ShouldThrowException() {
        // when & then
        assertThatThrownBy(() -> Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1200000)
                .deliveryDays(3)
                .ratingSnapshot(4.5)
                .purchaseMethod(PurchaseMethod.ANY) // ANY 불가
                .carrier(Carrier.SKT)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build())
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_PURCHASE_METHOD.getMessage());
    }

    @Test
    @DisplayName("번호이동 시 기존 통신사가 없으면 입찰 생성에 실패한다")
    void createBid_NumberTransferWithoutCurrentCarrier_ShouldThrowException() {
        // when & then
        assertThatThrownBy(() -> Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1200000)
                .deliveryDays(3)
                .ratingSnapshot(4.5)
                .purchaseMethod(PurchaseMethod.NUMBER_TRANSFER)
                .carrier(Carrier.SKT)
                .currentCarrier(null) // 번호이동인데 기존 통신사 없음
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build())
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.MISSING_CURRENT_CARRIER.getMessage());
    }

    @Test
    @DisplayName("견적이 입찰 가능한 상태일 때 입찰을 수정할 수 있다")
    void updateBid_WhenQuoteIsOpen() {
        // given
        Bid bid = createTestBid();

        // when & then
        assertThat(bid.canModify()).isTrue();
        
        assertThatNoException().isThrownBy(() -> 
            bid.updateBid(1150000, 2)
        );
        
        assertThat(bid.getPrice()).isEqualTo(1150000);
        assertThat(bid.getDeliveryDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("견적이 마감된 경우 입찰을 수정할 수 없다")
    void updateBid_WhenQuoteIsClosed() {
        // given
        Bid bid = createTestBid();
        quote.close();

        // when & then
        assertThat(bid.canModify()).isFalse();
        
        assertThatThrownBy(() -> bid.updateBid(1000000, 1))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.BID_MODIFICATION_NOT_ALLOWED.getMessage());
    }

    // 헬퍼 메서드들
    private Address createTestAddress() {
        return Address.builder()
                .postalCode("12345")
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("ABC빌딩 2층")
                .build();
    }

    private PricePlan createTestPricePlan() {
        return PricePlan.builder()
                .planName("5G 스탠다드")
                .planPrice(75000)
                .build();
    }

    private Bid createTestBid() {
        return Bid.builder()
                .quote(quote)
                .seller(seller)
                .price(1200000)
                .deliveryDays(3)
                .ratingSnapshot(4.5)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .carrier(Carrier.SKT)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .additionalSubsidy(50000)
                .installmentPrincipal(1000000)
                .additionalServices("보험, 액세서리")
                .pricePlan(createTestPricePlan())
                .contractMonths(24)
                .build();
    }
} 