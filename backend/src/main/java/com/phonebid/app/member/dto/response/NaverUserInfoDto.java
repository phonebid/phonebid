package com.phonebid.app.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 네이버 사용자 정보 DTO
 * 네이버 API에서 받은 사용자 정보를 담는 클래스
 * 
 * 네이버에서 제공하는 정보:
 * - 이메일 (필수)
 * - 이름 (필수) 
 * - 휴대전화번호 (필수)
 * - 별명 (추가)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverUserInfoDto {
    
    private String id;
    private String email;
    private String name;
    private String phone;
    private String nickname;
    
    public static NaverUserInfoDto of(String id, String email, String name, String phone, String nickname) {
        return NaverUserInfoDto.builder()
                .id(id)
                .email(email)
                .name(name)
                .phone(phone)
                .nickname(nickname)
                .build();
    }
} 