package com.phonebid.app.auction.domain;

import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Provider;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quote 엔티티 테스트")
class QuoteTest {

    @Test
    @DisplayName("Quote 객체를 생성할 수 있다")
    void createQuote() {
        // given
        User user = createTestUser();
        String model = "iPhone 16";
        String storage = "128GB";
        Carrier carrier = Carrier.SKT;
        String color = "블랙";
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(24);

        // when
        Quote quote = Quote.builder()
                .user(user)
                .model(model)
                .storage(storage)
                .carrier(carrier)
                .color(color)
                .expiredAt(expiredAt)
                .purchaseMethod(PurchaseMethod.ANY)
                .currentCarrier(null)
                .activationMethod(ActivationMethod.ANY)
                .build();

        // then
        assertThat(quote.getUser()).isEqualTo(user);
        assertThat(quote.getModel()).isEqualTo(model);
        assertThat(quote.getStorage()).isEqualTo(storage);
        assertThat(quote.getCarrier()).isEqualTo(carrier);
        assertThat(quote.getColor()).isEqualTo(color);
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.OPEN);
        assertThat(quote.getExpiredAt()).isEqualTo(expiredAt);
        assertThat(quote.getPurchaseMethod()).isEqualTo(PurchaseMethod.ANY);
        assertThat(quote.getCurrentCarrier()).isNull();
        assertThat(quote.getActivationMethod()).isEqualTo(ActivationMethod.ANY);
    }

    @Test
    @DisplayName("기본 생성 시 상태는 OPEN이고 24시간 후 만료된다")
    void defaultStatusAndExpiration() {
        // given
        User user = createTestUser();
        
        // when
        Quote quote = createTestQuote(user);

        // then
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.OPEN);
        assertThat(quote.canReceiveBids()).isTrue();
        assertThat(quote.canSelectBid()).isTrue();
    }

    @Test
    @DisplayName("만료된 견적은 입찰을 받을 수 없다")
    void expiredQuoteCannotReceiveBids() {
        // given
        User user = createTestUser();
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        Quote quote = Quote.builder()
                .user(user)
                .model("iPhone 16")
                .storage("128GB")
                .carrier(Carrier.SKT)
                .color("블랙")
                .expiredAt(pastTime)
                .build();

        // when & then
        assertThat(quote.isExpired()).isTrue();
        assertThat(quote.canReceiveBids()).isFalse();
        assertThat(quote.canSelectBid()).isFalse();
    }

    @Test
    @DisplayName("견적을 마감할 수 있다")
    void closeQuote() {
        // given
        User user = createTestUser();
        Quote quote = createTestQuote(user);

        // when
        quote.close();

        // then
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CLOSED);
        assertThat(quote.canReceiveBids()).isFalse();
        assertThat(quote.canSelectBid()).isFalse();
    }

    @Test
    @DisplayName("견적을 계약 완료 상태로 변경할 수 있다")
    void contractQuote() {
        // given
        User user = createTestUser();
        Quote quote = createTestQuote(user);

        // when
        quote.contract();

        // then
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CONTRACTED);
        assertThat(quote.canReceiveBids()).isFalse();
        assertThat(quote.canSelectBid()).isFalse();
    }

    @Test
    @DisplayName("진행중이 아닌 견적은 마감할 수 없다")
    void cannotCloseNonOpenQuote() {
        // given
        User user = createTestUser();
        Quote quote = createTestQuote(user);
        quote.close(); // 이미 마감됨

        // when & then
        assertThatThrownBy(quote::close)
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_QUOTE_STATUS.getMessage());
    }

    @Test
    @DisplayName("만료된 견적은 계약할 수 없다")
    void cannotContractExpiredQuote() {
        // given
        User user = createTestUser();
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        Quote quote = Quote.builder()
                .user(user)
                .model("iPhone 16")
                .storage("128GB")
                .carrier(Carrier.SKT)
                .color("블랙")
                .expiredAt(pastTime)
                .build();

        // when & then
        assertThatThrownBy(quote::contract)
                    .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_QUOTE_STATUS.getMessage());
    }

    @Test
    @DisplayName("마감 시간을 연장할 수 있다")
    void extendExpiration() {
        // given
        User user = createTestUser();
        Quote quote = createTestQuote(user);
        LocalDateTime newExpiredAt = LocalDateTime.now().plusDays(2);

        // when
        quote.extendExpiration(newExpiredAt);

        // then
        assertThat(quote.getExpiredAt()).isEqualTo(newExpiredAt);
    }

    @Test
    @DisplayName("과거 시간으로는 마감 시간을 연장할 수 없다")
    void cannotExtendExpirationToPast() {
        // given
        User user = createTestUser();
        Quote quote = createTestQuote(user);
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        // when & then
        assertThatThrownBy(() -> quote.extendExpiration(pastTime))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_END_TIME.getMessage());
    }

    @Test
    @DisplayName("전체 스펙 정보를 조회할 수 있다")
    void getFullSpecification() {
        // given
        User user = createTestUser();
        Quote quote = createTestQuote(user);

        // when
        String specification = quote.getFullSpecification();

        // then
        assertThat(specification).isEqualTo("iPhone 16 128GB SK텔레콤 블랙");
    }

    @Test
    @DisplayName("번호이동 구매방법으로 견적을 생성할 수 있다")
    void createQuoteWithNumberTransfer() {
        // given
        User user = createTestUser();

        // when
        Quote quote = Quote.builder()
                .user(user)
                .model("Galaxy S24")
                .storage("256GB")
                .carrier(Carrier.KT) // 이동할 통신사
                .color("화이트")
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.NUMBER_TRANSFER)
                .currentCarrier(Carrier.SKT) // 기존 통신사
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build();

        // then
        assertThat(quote.getPurchaseMethod()).isEqualTo(PurchaseMethod.NUMBER_TRANSFER);
        assertThat(quote.getCurrentCarrier()).isEqualTo(Carrier.SKT);
        assertThat(quote.getCarrier()).isEqualTo(Carrier.KT);
        assertThat(quote.getActivationMethod()).isEqualTo(ActivationMethod.SELECTIVE_SUBSIDY);
    }

    @Test
    @DisplayName("기기변경 구매방법으로 견적을 생성할 수 있다")
    void createQuoteWithDeviceChange() {
        // given
        User user = createTestUser();

        // when
        Quote quote = Quote.builder()
                .user(user)
                .model("iPhone 16 Pro")
                .storage("512GB")
                .carrier(Carrier.LGU) // 동일 통신사
                .color("골드")
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .currentCarrier(Carrier.LGU) // 기존 통신사 (동일)
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .build();

        // then
        assertThat(quote.getPurchaseMethod()).isEqualTo(PurchaseMethod.DEVICE_CHANGE);
        assertThat(quote.getCurrentCarrier()).isEqualTo(Carrier.LGU);
        assertThat(quote.getCarrier()).isEqualTo(Carrier.LGU);
        assertThat(quote.getActivationMethod()).isEqualTo(ActivationMethod.COMMON_SUBSIDY);
    }

    @Test
    @DisplayName("신규가입 구매방법으로 견적을 생성할 수 있다")
    void createQuoteWithNewSubscription() {
        // given
        User user = createTestUser();

        // when
        Quote quote = Quote.builder()
                .user(user)
                .model("iPhone 16 Plus")
                .storage("128GB")
                .carrier(Carrier.SKT) // 신규 가입할 통신사
                .color("블루")
                .expiredAt(LocalDateTime.now().plusHours(24))
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .currentCarrier(null) // 신규는 기존 통신사 없음
                .activationMethod(ActivationMethod.ANY)
                .build();

        // then
        assertThat(quote.getPurchaseMethod()).isEqualTo(PurchaseMethod.NEW_SUBSCRIPTION);
        assertThat(quote.getCurrentCarrier()).isNull();
        assertThat(quote.getCarrier()).isEqualTo(Carrier.SKT);
        assertThat(quote.getActivationMethod()).isEqualTo(ActivationMethod.ANY);
    }

    private User createTestUser() {
        return User.builder()
                .email("consumer@example.com")
                .name("소비자")
                .role(Role.CONSUMER)
                .provider(Provider.KAKAO)
                .providerId("kakao789")
                .build();
    }

    private Quote createTestQuote(User user) {
        return Quote.builder()
                .user(user)
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
}
