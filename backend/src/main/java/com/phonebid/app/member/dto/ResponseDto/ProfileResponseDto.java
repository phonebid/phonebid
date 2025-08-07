package com.phonebid.app.member.dto.ResponseDto;

import com.phonebid.app.member.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ProfileResponseDto {

    private UUID id;
    private String username;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String role;
    private String provider;
    private String providerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponseDto from(User user) {
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.email = user.getEmail();
        dto.name = user.getName();
        dto.nickname = user.getNickname();
        dto.phone = user.getPhone();
        dto.role = user.getRole().name();
        dto.provider = user.getProvider() != null ? user.getProvider().name() : null;
        dto.providerId = user.getProviderId();
        dto.createdAt = user.getCreatedAt();
        dto.updatedAt = user.getUpdatedAt();
        return dto;
    }
} 