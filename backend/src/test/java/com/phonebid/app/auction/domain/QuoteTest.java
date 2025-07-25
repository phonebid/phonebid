package com.phonebid.app.auction.domain;

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
                .build();

        // then
        assertThat(quote.getUser()).isEqualTo(user);
        assertThat(quote.getModel()).isEqualTo(model);
        assertThat(quote.getStorage()).isEqualTo(storage);
        assertThat(quote.getCarrier()).isEqualTo(carrier);
        assertThat(quote.getColor()).isEqualTo(color);
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.OPEN);
        assertThat(quote.getExpiredAt()).isEqualTo(expiredAt);
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
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("진행중인 견적만 마감할 수 있습니다.");
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
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("입찰 선택이 불가능한 견적입니다.");
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("마감 시간은 현재 시간보다 이후여야 합니다.");
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
                .build();
    }
}
