package com.phonebid.app.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "identity_verification_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ci", nullable = true)
    @Comment("연계정보 (CI)")
    private String ci;

    @Column(name = "di", nullable = true)
    @Comment("중복가입확인정보 (DI)")
    private String di;

    @Column(name = "verified_name", nullable = false)
    private String verifiedName;

    @Column(name = "verified_phone", nullable = false)
    private String verifiedPhone;

    @Column(name = "verified_birth", nullable = true)
    private String verifiedBirth;

    @Column(name = "carrier", nullable = true)
    @Comment("통신사 (원본 문자열)")
    private String carrier;

    @Column(name = "provider", nullable = true)
    @Comment("인증기관 (NICE, KMC, PASS 등)")
    private String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public IdentityVerificationLog(User user, String ci, String di,
            String verifiedName, String verifiedPhone, String verifiedBirth,
            String carrier, String provider) {
        this.user = user;
        this.ci = ci;
        this.di = di;
        this.verifiedName = verifiedName;
        this.verifiedPhone = verifiedPhone;
        this.verifiedBirth = verifiedBirth;
        this.carrier = carrier;
        this.provider = provider;
    }
}
