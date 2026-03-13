package com.phonebid.app.member.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 * 로그인 성공 시 반환되는 데이터를 담는 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    private String accessToken;
    
    @JsonIgnore // JSON 직렬화 시 제외 (쿠키로만 전달)
    private String refreshToken;
    
    private String tokenType;
    private String username;
    private String nickname;
    private String role;
    private Boolean isIdentityVerified;

    public static LoginResponseDto of(String accessToken, String username, String nickname, String role, Boolean isIdentityVerified) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .username(username)
                .nickname(nickname)
                .role(role)
                .isIdentityVerified(isIdentityVerified)
                .build();
    }

    public static LoginResponseDto of(String accessToken, String refreshToken, String username, String nickname, String role, Boolean isIdentityVerified) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(username)
                .nickname(nickname)
                .role(role)
                .isIdentityVerified(isIdentityVerified)
                .build();
    }
} 