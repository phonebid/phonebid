package com.phonebid.app.auth.domain;

import com.phonebid.app.common.domain.BaseTimeEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 엔티티
 * 
 * immutable한 엔티티로, 생성 후 수정되지 않습니다.
 * 만료되거나 삭제될 뿐 업데이트 로직이 없으므로 BaseTimeEntity를 상속합니다.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user_deleted", columnList = "user_id, deleted_at"),
    @Index(name = "idx_refresh_tokens_token", columnList = "token"),
    @Index(name = "idx_refresh_tokens_expires_deleted", columnList = "expires_at, deleted_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("Refresh Token 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("사용자")
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    @Comment("Refresh Token 값")
    private String token;

    @Column(name = "expires_at", nullable = false)
    @Comment("만료 시각")
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isDeleted();
    }
}

