package com.phonebid.app.auction.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PricePlan 임베디드 타입 테스트")
class PricePlanTest {

    @Test
    @DisplayName("완전한 요금제 정보로 PricePlan을 생성할 수 있다")
    void createCompletePricePlan() {
        // when
        PricePlan pricePlan = PricePlan.builder()
                .planName("5G 프리미엄")
                .planPrice(89000)
                .build();

        // then
        assertThat(pricePlan.getPlanName()).isEqualTo("5G 프리미엄");
        assertThat(pricePlan.getPlanPrice()).isEqualTo(89000);
        assertThat(pricePlan.isComplete()).isTrue();
        assertThat(pricePlan.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("요금제 요약 정보를 올바르게 생성한다")
    void getPlanSummary_WithCompleteInfo() {
        // given
        PricePlan pricePlan = PricePlan.builder()
                .planName("5G 스탠다드")
                .planPrice(75000)
                .build();

        // when
        String summary = pricePlan.getPlanSummary();

        // then
        assertThat(summary).isEqualTo("5G 스탠다드 (75,000원)");
    }

    @Test
    @DisplayName("정보가 없는 경우 요약은 '요금제 정보 없음'을 반환한다")
    void getPlanSummary_WithNoInfo() {
        // given
        PricePlan pricePlan = PricePlan.builder()
                .planName(null)
                .planPrice(null)
                .build();

        // when
        String summary = pricePlan.getPlanSummary();

        // then
        assertThat(summary).isEqualTo("요금제 정보 없음");
    }



    @Test
    @DisplayName("요금제 정보가 완전하지 않은 경우 isComplete는 false를 반환한다")
    void isComplete_WithIncompleteInfo() {
        // given
        PricePlan planWithoutName = PricePlan.builder()
                .planName(null)
                .planPrice(75000)
                .build();

        PricePlan planWithoutPrice = PricePlan.builder()
                .planName("5G 스탠다드")
                .planPrice(null)
                .build();

        PricePlan planWithZeroPrice = PricePlan.builder()
                .planName("5G 스탠다드")
                .planPrice(0)
                .build();

        // when & then
        assertThat(planWithoutName.isComplete()).isFalse();
        assertThat(planWithoutPrice.isComplete()).isFalse();
        assertThat(planWithZeroPrice.isComplete()).isFalse();
    }

    @Test
    @DisplayName("모든 정보가 비어있으면 isEmpty는 true를 반환한다")
    void isEmpty_WithAllEmptyInfo() {
        // given
        PricePlan pricePlan = PricePlan.builder()
                .planName(null)
                .planPrice(null)
                .build();

        // when & then
        assertThat(pricePlan.isEmpty()).isTrue();
        assertThat(pricePlan.isComplete()).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 요금제 이름으로도 생성할 수 있다")
    void isEmpty_WithEmptyPlanName() {
        // when
        PricePlan pricePlan = PricePlan.builder()
                .planName("  ")
                .planPrice(0)
                .build();

        // then
        assertThat(pricePlan.isEmpty()).isTrue();
        assertThat(pricePlan.isComplete()).isFalse();
    }

    @Test
    @DisplayName("요금제 가격이 음수여도 생성할 수 있다")
    void createPricePlan_WithNegativePrice() {
        // when
        PricePlan pricePlan = PricePlan.builder()
                .planName("5G 스탠다드")
                .planPrice(-1000) // 음수
                .build();

        // then
        assertThat(pricePlan.getPlanName()).isEqualTo("5G 스탠다드");
        assertThat(pricePlan.getPlanPrice()).isEqualTo(-1000);
        assertThat(pricePlan.isComplete()).isFalse(); // 음수이므로 완전하지 않음
    }

    @Test
    @DisplayName("요금제 이름이 길어도 생성할 수 있다")
    void createPricePlan_WithLongPlanName() {
        // given
        String longPlanName = "a".repeat(101); // 101자

        // when
        PricePlan pricePlan = PricePlan.builder()
                .planName(longPlanName)
                .planPrice(75000)
                .build();

        // then
        assertThat(pricePlan.getPlanName()).isEqualTo(longPlanName);
        assertThat(pricePlan.getPlanPrice()).isEqualTo(75000);
    }

    @Test
    @DisplayName("null 값들로는 정상적으로 생성할 수 있다")
    void createPricePlan_WithNullValues_ShouldSucceed() {
        // when & then
        assertThatNoException().isThrownBy(() -> {
            PricePlan pricePlan = PricePlan.builder()
                .planName(null)
                .planPrice(null)
                .build();

            assertThat(pricePlan.isEmpty()).isTrue();
            assertThat(pricePlan.isComplete()).isFalse();
        });
    }
} 