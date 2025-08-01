package com.phonebid.app.member.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 사용자 정보 DTO
 * 카카오 API에서 받은 사용자 정보를 담는 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoDto {
    
    private Long id;
    private String nickname;
    private String email;
    private String name;
    
    public static KakaoUserInfoDto of(Long id, String nickname, String email, String name) {
        return KakaoUserInfoDto.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .name(name)
                .build();
    }
} 