package com.phonebid.app.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;


    @Builder
    public User(String email, String name, Role role, Provider provider, String providerId) {
        this.email = email;
        this.name = name;
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

    public void updateRole(Role role) {
        this.role = role;
    }
} 