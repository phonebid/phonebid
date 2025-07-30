package com.phonebid.app.member.dto.ResponseDto;

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
    private String tokenType;
    private String username;
    private String nickname;
    private String role;
    
    public static LoginResponseDto of(String accessToken, String username, String nickname, String role) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .username(username)
                .nickname(nickname)
                .role(role)
                .build();
    }
} 