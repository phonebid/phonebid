package com.phonebid.app.member;

import com.phonebid.app.member.domain.ApprovalStatus;
import com.phonebid.app.member.domain.Provider;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Seller 엔티티 테스트")
class SellerTest {

    @Test
    @DisplayName("Seller 객체를 생성할 수 있다")
    void createSeller() {
        // given
        User user = createTestUser();
        String businessNumber = "123-45-67890";
        String storeName = "테스트 스토어";

        // when
        Seller seller = Seller.builder()
                .user(user)
                .businessNumber(businessNumber)
                .storeName(storeName)
                .build();

        // then
        assertThat(seller.getUser()).isEqualTo(user);
        assertThat(seller.getUserId()).isEqualTo(user.getId());
        assertThat(seller.getBusinessNumber()).isEqualTo(businessNumber);
        assertThat(seller.getStoreName()).isEqualTo(storeName);
        assertThat(seller.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("기본 생성 시 승인 상태는 PENDING이다")
    void defaultApprovalStatusIsPending() {
        // given
        User user = createTestUser();
        
        // when
        Seller seller = createTestSeller(user);

        // then
        assertThat(seller.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(seller.isPendingApproval()).isTrue();
        assertThat(seller.canSell()).isFalse();
    }

    @Test
    @DisplayName("판매자를 승인할 수 있다")
    void approveSeller() {
        // given
        User user = createTestUser();
        Seller seller = createTestSeller(user);

        // when
        seller.approve();

        // then
        assertThat(seller.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(seller.canSell()).isTrue();
        assertThat(seller.isPendingApproval()).isFalse();
    }

    @Test
    @DisplayName("판매자를 거부할 수 있다")
    void rejectSeller() {
        // given
        User user = createTestUser();
        Seller seller = createTestSeller(user);

        // when
        seller.reject();

        // then
        assertThat(seller.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(seller.canSell()).isFalse();
        assertThat(seller.isPendingApproval()).isFalse();
    }

    @Test
    @DisplayName("승인 대기 상태가 아닌 판매자는 승인할 수 없다")
    void cannotApproveNonPendingSeller() {
        // given
        User user = createTestUser();
        Seller seller = createTestSeller(user);
        seller.approve(); // 이미 승인됨

        // when & then
        assertThatThrownBy(seller::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("승인 대기 상태가 아닌 판매자는 승인할 수 없습니다.");
    }

    @Test
    @DisplayName("승인 대기 상태가 아닌 판매자는 거부할 수 없다")
    void cannotRejectNonPendingSeller() {
        // given
        User user = createTestUser();
        Seller seller = createTestSeller(user);
        seller.approve(); // 이미 승인됨

        // when & then
        assertThatThrownBy(seller::reject)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("승인 대기 상태가 아닌 판매자는 거부할 수 없습니다.");
    }

    @Test
    @DisplayName("상호명을 업데이트할 수 있다")
    void updateStoreName() {
        // given
        User user = createTestUser();
        Seller seller = createTestSeller(user);
        String newStoreName = "새로운 스토어";

        // when
        seller.updateStoreName(newStoreName);

        // then
        assertThat(seller.getStoreName()).isEqualTo(newStoreName);
    }

    @Test
    @DisplayName("사업자등록번호를 업데이트할 수 있다")
    void updateBusinessNumber() {
        // given
        User user = createTestUser();
        Seller seller = createTestSeller(user);
        String newBusinessNumber = "987-65-43210";

        // when
        seller.updateBusinessNumber(newBusinessNumber);

        // then
        assertThat(seller.getBusinessNumber()).isEqualTo(newBusinessNumber);
    }

    private User createTestUser() {
        return User.builder()
                .email("seller@example.com")
                .name("판매자")
                .role(Role.SELLER)
                .provider(Provider.KAKAO)
                .providerId("kakao456")
                .build();
    }

    private Seller createTestSeller(User user) {
        return Seller.builder()
                .user(user)
                .businessNumber("123-45-67890")
                .storeName("테스트 스토어")
                .build();
    }
} 