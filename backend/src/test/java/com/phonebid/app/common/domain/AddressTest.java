package com.phonebid.app.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address 임베디드 타입 테스트")
class AddressTest {

    @Test
    @DisplayName("완전한 주소 정보로 Address를 생성할 수 있다")
    void createCompleteAddress() {
        // when
        Address address = Address.builder()
                .postalCode("12345")
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("ABC빌딩 2층")
                .build();

        // then
        assertThat(address.getPostalCode()).isEqualTo("12345");
        assertThat(address.getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(address.getDetailAddress()).isEqualTo("ABC빌딩 2층");
        assertThat(address.isComplete()).isTrue();
        assertThat(address.hasDetailAddress()).isTrue();
        assertThat(address.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("상세주소 없이도 Address를 생성할 수 있다")
    void createAddressWithoutDetailAddress() {
        // when
        Address address = Address.builder()
                .postalCode("54321")
                .address("부산시 해운대구 해운대로 456")
                .detailAddress(null)
                .build();

        // then
        assertThat(address.getPostalCode()).isEqualTo("54321");
        assertThat(address.getAddress()).isEqualTo("부산시 해운대구 해운대로 456");
        assertThat(address.getDetailAddress()).isNull();
        assertThat(address.isComplete()).isTrue();
        assertThat(address.hasDetailAddress()).isFalse();
        assertThat(address.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("전체 주소 문자열을 올바르게 생성한다")
    void getFullAddress_WithCompleteInfo() {
        // given
        Address address = Address.builder()
                .postalCode("12345")
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("ABC빌딩 2층")
                .build();

        // when
        String fullAddress = address.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("(12345) 서울시 강남구 테헤란로 123 ABC빌딩 2층");
    }

    @Test
    @DisplayName("상세주소가 없는 경우 전체 주소 문자열을 올바르게 생성한다")
    void getFullAddress_WithoutDetailAddress() {
        // given
        Address address = Address.builder()
                .postalCode("54321")
                .address("부산시 해운대구 해운대로 456")
                .detailAddress(null)
                .build();

        // when
        String fullAddress = address.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("(54321) 부산시 해운대구 해운대로 456");
    }

    @Test
    @DisplayName("우편번호가 없는 경우에도 주소 문자열을 생성한다")
    void getFullAddress_WithoutPostalCode() {
        // given
        Address address = Address.builder()
                .postalCode(null)
                .address("서울시 종로구 종로 1")
                .detailAddress("지하 1층")
                .build();

        // when
        String fullAddress = address.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("서울시 종로구 종로 1 지하 1층");
    }

    @Test
    @DisplayName("빈 문자열 상세주소는 상세주소가 없는 것으로 처리한다")
    void hasDetailAddress_WithEmptyString() {
        // given
        Address address = Address.builder()
                .postalCode("12345")
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("   ") // 공백만 있는 문자열
                .build();

        // when & then
        assertThat(address.hasDetailAddress()).isFalse();
    }

    @Test
    @DisplayName("주소가 완전하지 않은 경우 isComplete는 false를 반환한다")
    void isComplete_WithIncompleteAddress() {
        // given
        Address address1 = Address.builder()
                .postalCode(null)
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("ABC빌딩 2층")
                .build();

        Address address2 = Address.builder()
                .postalCode("12345")
                .address(null)
                .detailAddress("ABC빌딩 2층")
                .build();

        // when & then
        assertThat(address1.isComplete()).isFalse();
        assertThat(address2.isComplete()).isFalse();
    }

    @Test
    @DisplayName("모든 필드가 비어있으면 isEmpty는 true를 반환한다")
    void isEmpty_WithAllEmptyFields() {
        // given
        Address address = Address.builder()
                .postalCode(null)
                .address(null)
                .detailAddress(null)
                .build();

        // when & then
        assertThat(address.isEmpty()).isTrue();
        assertThat(address.isComplete()).isFalse();
    }

    @Test
    @DisplayName("빈 문자열들도 비어있는 것으로 처리한다")
    void isEmpty_WithEmptyStrings() {
        // given
        Address address = Address.builder()
                .postalCode("  ")
                .address("")
                .detailAddress("   ")
                .build();

        // when & then
        assertThat(address.isEmpty()).isTrue();
        assertThat(address.isComplete()).isFalse();
    }

    @Test
    @DisplayName("하나라도 값이 있으면 isEmpty는 false를 반환한다")
    void isEmpty_WithSomeValue() {
        // given
        Address address = Address.builder()
                .postalCode(null)
                .address("서울시 강남구")
                .detailAddress(null)
                .build();

        // when & then
        assertThat(address.isEmpty()).isFalse();
    }
} 