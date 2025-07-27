package com.phonebid.app.auction.domain;

import com.phonebid.app.member.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class BidHistoryTest {

    private User consumer;
    private User sellerUser;
    private Seller seller;
    private Quote quote;
    private Bid bid;

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
                .build();

        quote = Quote.builder()
                .user(consumer)
                .model("iPhone 16")
                .storage("128GB")
                .carrier(Carrier.SKT)
                .color("블랙")
                .expiredAt(LocalDateTime.now().plusHours(24))
                .build();

        bid = Bid.builder()
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
                .additionalServices("보험")
                .pricePlan(PricePlan.builder().planName("5G 스탠다드").planPrice(75000).build())
                .contractMonths(24)
                .build();
    }

    @Test
    @DisplayName("입찰로부터 히스토리를 생성할 수 있다")
    void createFromBid_ShouldCreateHistoryWithBidData() {
        // When
        BidHistory history = BidHistory.createFromBid(bid, 1);

        // Then
        assertThat(history.getBid()).isEqualTo(bid);
        assertThat(history.getVersion()).isEqualTo(1);
        assertThat(history.getPrice()).isEqualTo(bid.getPrice());
        assertThat(history.getDeliveryDays()).isEqualTo(bid.getDeliveryDays());
    }

    @Test
    @DisplayName("최신 버전인지 확인할 수 있다")
    void isLatestVersion_ShouldReturnCorrectResult() {
        // Given
        BidHistory history = BidHistory.createFromBid(bid, 2);

        // When & Then
        assertThat(history.isLatestVersion(2)).isTrue();
        assertThat(history.isLatestVersion(3)).isFalse();
        assertThat(history.isLatestVersion(1)).isFalse();
    }

    @Test
    @DisplayName("히스토리 요약 정보를 올바르게 생성한다")
    void getHistorySummary_ShouldReturnCorrectFormat() {
        // Given
        BidHistory history = BidHistory.createFromBid(bid, 1);

        // When
        String summary = history.getHistorySummary();

        // Then
        assertThat(summary).isEqualTo("v1: 1,200,000원, 3일");
    }

    @Test
    @DisplayName("가격 변경 여부를 확인할 수 있다")
    void hasPriceChanged_ShouldDetectPriceChange() {
        // Given
        BidHistory history = BidHistory.createFromBid(bid, 1);

        // When & Then
        assertThat(history.hasPriceChanged(1200000)).isFalse(); // 동일한 가격
        assertThat(history.hasPriceChanged(1150000)).isTrue();  // 다른 가격
    }

    @Test
    @DisplayName("배송일 변경 여부를 확인할 수 있다")
    void hasDeliveryDaysChanged_ShouldDetectDeliveryDaysChange() {
        // Given
        BidHistory history = BidHistory.createFromBid(bid, 1);

        // When & Then
        assertThat(history.hasDeliveryDaysChanged(3)).isFalse(); // 동일한 배송일
        assertThat(history.hasDeliveryDaysChanged(2)).isTrue();  // 다른 배송일
    }

    @Test
    @DisplayName("Builder 패턴으로 직접 생성할 수 있다")
    void builder_ShouldCreateBidHistory() {
        // When
        BidHistory history = BidHistory.builder()
                .bid(bid)
                .version(1)
                .price(1100000)
                .deliveryDays(2)
                .build();

        // Then
        assertThat(history.getBid()).isEqualTo(bid);
        assertThat(history.getVersion()).isEqualTo(1);
        assertThat(history.getPrice()).isEqualTo(1100000);
        assertThat(history.getDeliveryDays()).isEqualTo(2);
    }
} 