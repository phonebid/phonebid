package com.phonebid.app.auction.domain;

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
        seller.approve(); // 승인된 판매자로 설정

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
                .build();
    }

    @Test
    @DisplayName("입찰을 정상적으로 생성할 수 있다")
    void createBid_ShouldBeCreatedSuccessfully() {
        // Then
        assertThat(bid.getQuote()).isEqualTo(quote);
        assertThat(bid.getSeller()).isEqualTo(seller);
        assertThat(bid.getPrice()).isEqualTo(1200000);
        assertThat(bid.getDeliveryDays()).isEqualTo(3);
        assertThat(bid.getRatingSnapshot()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("견적이 입찰 가능한 상태일 때 입찰을 수정할 수 있다")
    void activeBid_CanBeModified() {
        // When & Then
        assertThat(bid.canModify()).isTrue();
        
        // 입찰 수정
        assertThatNoException().isThrownBy(() -> 
            bid.updateBid(1150000, 2)
        );
        
        assertThat(bid.getPrice()).isEqualTo(1150000);
        assertThat(bid.getDeliveryDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("견적이 마감된 경우 입찰을 수정할 수 없다")
    void expiredQuote_BidCannotBeModified() {
        // Given
        quote.close();

        // When & Then
        assertThat(bid.canModify()).isFalse();
        
        assertThatThrownBy(() -> bid.updateBid(1000000, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("수정할 수 없는 입찰입니다.");
    }

    @Test
    @DisplayName("입찰 요약 정보를 올바르게 생성한다")
    void getBidSummary_ShouldReturnCorrectFormat() {
        // When
        String summary = bid.getBidSummary();

        // Then
        assertThat(summary).isEqualTo("입찰가: 1,200,000원, 배송예정: 3일");
    }

    @Test
    @DisplayName("입찰 수정 시 가격과 배송일이 모두 변경된다")
    void updateBid_ShouldUpdateBothPriceAndDeliveryDays() {
        // Given
        Integer newPrice = 1100000;
        Integer newDeliveryDays = 2;

        // When
        bid.updateBid(newPrice, newDeliveryDays);

        // Then
        assertThat(bid.getPrice()).isEqualTo(newPrice);
        assertThat(bid.getDeliveryDays()).isEqualTo(newDeliveryDays);
    }
} 