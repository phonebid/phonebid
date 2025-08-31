package com.phonebid.app.member.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Comment;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("사용자 고유 ID (UUID)")
    private UUID id;

    @Size(min = 4, max = 255, message = "유저ID는 4자 이상 255자 이하여야 합니다")  // 길이를 4~255자로 확장
    @Pattern(regexp = "^[a-z0-9@._-]+$", message = "유저ID는 소문자, 숫자, @, ., _, -만 사용 가능합니다")  // 이메일 형식 허용
    @Column(name = "username", nullable = false, unique = true)
    @Comment("사용자명 (로그인 ID)")
    private String username;

    @Column(name = "password", nullable = false)
    @Comment("암호화된 비밀번호")
    private String password;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    @Comment("이메일 주소 (유니크)")
    private String email;

    @Column(name = "name", nullable = false)
    @Comment("실명")
    private String name;

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$", message = "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다")
    @Column(name = "nickname", nullable = false)
    @Comment("닉네임 (2-10자)")
    private String nickname;

    @Pattern(regexp = "^[0-9]+$", message = "휴대전화번호는 숫자만 입력 가능합니다")
    @Column(name = "phone", nullable = true)
    @Comment("휴대폰 번호")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Comment("사용자 역할 (CONSUMER, SELLER, ADMIN)")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = true)
    @Comment("소셜 로그인 제공자 (KAKAO, NAVER)")
    private Provider provider;

    @Column(name = "provider_id", nullable = true)
    @Comment("소셜 로그인 고유 ID")
    private String providerId;

    @Builder
    public User(String username, String password, String email, String name, String nickname, String phone, Role role, Provider provider, String providerId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    // 비즈니스 메서드
    public boolean isConsumer() {
        return this.role == Role.CONSUMER;
    }

    public boolean isSeller() {
        return this.role == Role.SELLER;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateProvider(Provider provider) {
        this.provider = provider;
    }

    public void updateProviderId(String providerId) {
        this.providerId = providerId;
    }

    // 논리적 삭제
    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }

    // 삭제 여부 확인
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDelete);
    }
}
