package com.phonebid.app.mypage.dto.response;

import com.phonebid.app.member.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileResponseDto {

    private String username;
    private String nickname;
    private String phone;
    private String name;
    private String profileImageUrl;

    public static ProfileResponseDto from(User user) {
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.username = user.getUsername();
        dto.nickname = user.getNickname();
        dto.phone = user.getPhone();
        dto.name = user.getName();
        dto.profileImageUrl = user.getProfileImageUrl();
        return dto;
    }
}

