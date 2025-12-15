package com.phonebid.app.mypage.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    /**
     * 프로필 조회
     * 활성 사용자의 프로필 정보를 조회하여 DTO로 변환하여 반환합니다.
     */
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(String username) {
        User user = loadActiveUser(username);
        return ProfileResponseDto.from(user);
    }

    /**
     * 프로필 수정
     * 사용자의 이름, 닉네임, 휴대폰 번호를 선택적으로 수정합니다.
     * 닉네임의 경우 중복 검증을 수행하며, 자신의 기존 닉네임은 제외합니다.
     */
    @Transactional
    public void updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        User user = loadActiveUser(username);

        String newName = requestDto.getName();
        String newNickname = requestDto.getNickname();
        String newPhone = requestDto.getPhone();

        if (newName != null && !newName.trim().isEmpty()) {
            user.updateName(newName.trim());
        }

        if (newNickname != null && !newNickname.trim().isEmpty()) {
            String trimmedNickname = newNickname.trim();
            Optional<User> checkNickname = userRepository.findByNickname(trimmedNickname);
            if (checkNickname.isPresent() && !checkNickname.get().getId().equals(user.getId())) {
                throw new CustomException(CommonErrorCode.DUPLICATE_NICKNAME);
            }
            user.updateNickname(trimmedNickname);
        }

        if (newPhone != null && !newPhone.trim().isEmpty()) {
            user.updatePhone(newPhone.trim());
        }
    }

    /**
     * 활성 사용자 조회
     * 삭제되지 않은 활성 사용자만 조회하며, 사용자가 없거나 삭제된 경우 예외를 발생시킵니다.
     */
    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
            .filter(user -> !user.isDeleted())
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }
}

