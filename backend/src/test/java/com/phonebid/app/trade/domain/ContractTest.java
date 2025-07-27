package com.phonebid.app.trade.domain;

import com.phonebid.app.auction.domain.*;
import com.phonebid.app.member.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class ContractTest {

    private User consumer;
    private User sellerUser;
    private Seller seller;
    private Quote quote;
    private Bid bid;
    private Contract contract;

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
        seller.approve();

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

        contract = Contract.builder()
                .quote(quote)
                .selectedBid(bid)
                .build();
    }

    @Test
    @DisplayName("계약 생성 시 기본 상태는 SIGNING이다")
    void createContract_ShouldHaveSigningStatus() {
        // Then
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.SIGNING);
        assertThat(contract.isPending()).isTrue();
        assertThat(contract.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("계약을 서명하면 상태가 SIGNED로 변경되고 견적 상태도 변경된다")
    void signContract_ShouldChangeStatusAndUpdateQuote() {
        // When
        contract.sign();

        // Then
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.SIGNED);
        assertThat(contract.isCompleted()).isTrue();
        assertThat(contract.getSignedAt()).isNotNull();
        
        // 견적 상태 변경 확인
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CONTRACTED);
    }

    @Test
    @DisplayName("계약을 취소하면 상태가 CANCELLED로 변경된다")
    void cancelContract_ShouldChangeStatusToCancelled() {
        // When
        contract.cancel();

        // Then
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.CANCELLED);
    }

    @Test
    @DisplayName("서명된 계약은 다시 서명할 수 없다")
    void signedContract_CannotBeSignedAgain() {
        // Given
        contract.sign();

        // When & Then
        assertThatThrownBy(() -> contract.sign())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("서명할 수 없는 계약 상태입니다");
    }

    @Test
    @DisplayName("서명된 계약은 취소할 수 없다")
    void signedContract_CannotBeCancelled() {
        // Given
        contract.sign();

        // When & Then
        assertThatThrownBy(() -> contract.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("취소할 수 없는 계약 상태입니다");
    }

    @Test
    @DisplayName("계약 금액은 선택된 입찰의 가격과 같다")
    void getContractAmount_ShouldReturnSelectedBidPrice() {
        // When
        Integer contractAmount = contract.getContractAmount();

        // Then
        assertThat(contractAmount).isEqualTo(bid.getPrice());
        assertThat(contractAmount).isEqualTo(1200000);
    }

    @Test
    @DisplayName("계약 요약 정보를 올바르게 생성한다")
    void getContractSummary_ShouldReturnCorrectFormat() {
        // When
        String summary = contract.getContractSummary();

        // Then
        assertThat(summary).isEqualTo("계약금액: 1,200,000원, 상태: 서명 대기");
    }

    @Test
    @DisplayName("입찰이 해당 견적에 속하지 않으면 계약을 생성할 수 없다")
    void createContract_WithMismatchedBidAndQuote_ShouldThrowException() {
        // Given
        Quote anotherQuote = Quote.builder()
                .user(consumer)
                .model("Galaxy S24")
                .storage("256GB")
                .carrier(Carrier.KT)
                .color("화이트")
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.ANY)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.ANY)
                .build();

        // When & Then
        assertThatThrownBy(() -> Contract.builder()
                .quote(anotherQuote)
                .selectedBid(bid)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("선택된 입찰이 해당 견적에 속하지 않습니다.");
    }

    @Test
    @DisplayName("견적이 입찰 선택 불가능한 상태면 계약을 생성할 수 없다")
    void createContract_WithClosedQuote_ShouldThrowException() {
        // Given
        quote.close();

        // When & Then
        assertThatThrownBy(() -> Contract.builder()
                .quote(quote)
                .selectedBid(bid)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("입찰 선택이 불가능한 견적입니다.");
    }
} 