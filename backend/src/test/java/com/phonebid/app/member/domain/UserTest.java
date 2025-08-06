package com.phonebid.app.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("User 객체를 생성할 수 있다")
    void createUser() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";
        Role role = Role.CONSUMER;
        Provider provider = Provider.KAKAO;
        String providerId = "kakao123";

        // when
        User user = User.builder()
                .email(email)
                .name(name)
                .role(role)
                .provider(provider)
                .providerId(providerId)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getProvider()).isEqualTo(provider);
        assertThat(user.getProviderId()).isEqualTo(providerId);
    }

    @Test
    @DisplayName("phone 필드가 포함된 User 객체를 생성할 수 있다")
    void createUserWithPhone() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";
        String phone = "010-1234-5678";
        Role role = Role.CONSUMER;
        Provider provider = Provider.NAVER;
        String providerId = "naver123";

        // when
        User user = User.builder()
                .email(email)
                .name(name)
                .phone(phone)
                .role(role)
                .provider(provider)
                .providerId(providerId)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getProvider()).isEqualTo(provider);
        assertThat(user.getProviderId()).isEqualTo(providerId);
    }

    @Test
    @DisplayName("phone 필드가 null인 User 객체를 생성할 수 있다")
    void createUserWithNullPhone() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";
        Role role = Role.CONSUMER;

        // when
        User user = User.builder()
                .email(email)
                .name(name)
                .phone(null)
                .role(role)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhone()).isNull();
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("사용자 전화번호를 업데이트할 수 있다")
    void updatePhone() {
        // given
        User user = createTestUser(Role.CONSUMER);
        String newPhone = "010-9876-5432";

        // when
        user.updatePhone(newPhone);

        // then
        assertThat(user.getPhone()).isEqualTo(newPhone);
    }

    @Test
    @DisplayName("사용자 전화번호를 null로 업데이트할 수 있다")
    void updatePhoneToNull() {
        // given
        User user = createTestUserWithPhone(Role.CONSUMER, "010-1234-5678");

        // when
        user.updatePhone(null);

        // then
        assertThat(user.getPhone()).isNull();
    }

    @Test
    @DisplayName("사용자 전화번호를 빈 문자열로 업데이트할 수 있다")
    void updatePhoneToEmpty() {
        // given
        User user = createTestUserWithPhone(Role.CONSUMER, "010-1234-5678");

        // when
        user.updatePhone("");

        // then
        assertThat(user.getPhone()).isEqualTo("");
    }

    @Test
    @DisplayName("Consumer 역할인지 확인할 수 있다")
    void isConsumer() {
        // given
        User consumerUser = createTestUser(Role.CONSUMER);
        User sellerUser = createTestUser(Role.SELLER);

        // when & then
        assertThat(consumerUser.isConsumer()).isTrue();
        assertThat(sellerUser.isConsumer()).isFalse();
    }

    @Test
    @DisplayName("Seller 역할인지 확인할 수 있다")
    void isSeller() {
        // given
        User consumerUser = createTestUser(Role.CONSUMER);
        User sellerUser = createTestUser(Role.SELLER);

        // when & then
        assertThat(consumerUser.isSeller()).isFalse();
        assertThat(sellerUser.isSeller()).isTrue();
    }

    @Test
    @DisplayName("Admin 역할인지 확인할 수 있다")
    void isAdmin() {
        // given
        User consumerUser = createTestUser(Role.CONSUMER);
        User adminUser = createTestUser(Role.ADMIN);

        // when & then
        assertThat(consumerUser.isAdmin()).isFalse();
        assertThat(adminUser.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("사용자 이름을 업데이트할 수 있다")
    void updateName() {
        // given
        User user = createTestUser(Role.CONSUMER);
        String newName = "새로운 이름";

        // when
        user.updateName(newName);

        // then
        assertThat(user.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("사용자 역할을 업데이트할 수 있다")
    void updateRole() {
        // given
        User user = createTestUser(Role.CONSUMER);
        Role newRole = Role.SELLER;

        // when
        user.updateRole(newRole);

        // then
        assertThat(user.getRole()).isEqualTo(newRole);
    }

    private User createTestUser(Role role) {
        return User.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(role)
                .provider(Provider.KAKAO)
                .providerId("kakao123")
                .build();
    }

    private User createTestUserWithPhone(Role role, String phone) {
        return User.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .phone(phone)
                .role(role)
                .provider(Provider.NAVER)
                .providerId("naver123")
                .build();
    }
} 