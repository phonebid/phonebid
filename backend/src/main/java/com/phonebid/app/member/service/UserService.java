package com.phonebid.app.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.dto.request.SignupRequestDto;
import com.phonebid.app.member.dto.request.LoginRequestDto;
import com.phonebid.app.member.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.member.dto.request.PasswordChangeRequestDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.dto.response.ProfileResponseDto;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원 가입
     */
    @Transactional
    public void signup(SignupRequestDto requestDto) {

        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());
        
        // 회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new CustomException(CommonErrorCode.DUPLICATE_USERNAME);
        }

        // email 중복확인
        String email = requestDto.getEmail();
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new CustomException(CommonErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복확인
        String nickname = requestDto.getNickname();
        Optional<User> checkNickname = userRepository.findByNickname(nickname);
        if (checkNickname.isPresent()) {
            throw new CustomException(CommonErrorCode.DUPLICATE_NICKNAME);
        }

        // 이름
        String name = requestDto.getName();

        // 회원 가입
        User user = User.builder()
            .username(username)
            .password(password)
            .email(email)
            .name(name)
            .nickname(nickname)
            .role(Role.CONSUMER) // 기본 회원가입은 소비자 역할
            .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        // 사용자 존재 확인
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(CommonErrorCode.INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성
        String token = jwtUtil.createToken(user.getUsername(), user.getRole());
        
        // LoginResponseDto 생성 및 반환
        return LoginResponseDto.of(
            token, 
            user.getUsername(), 
            user.getNickname(), 
            user.getRole().name()
        );
    }

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
        
        if (user.isDeleted()) {
            throw new CustomException(CommonErrorCode.USER_NOT_FOUND);
        }
        
        return ProfileResponseDto.from(user);
    }

    /**
     * 내 정보 수정 (닉네임만 수정 가능)
     */
    @Transactional
    public void updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
        
        if (user.isDeleted()) {
            throw new CustomException(CommonErrorCode.USER_NOT_FOUND);
        }

        String newNickname = requestDto.getNickname();
        
        // 닉네임 중복 확인 (자신의 기존 닉네임은 제외)
        Optional<User> checkNickname = userRepository.findByNickname(newNickname);
        if (checkNickname.isPresent() && !checkNickname.get().getId().equals(user.getId())) {
            throw new CustomException(CommonErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateNickname(newNickname);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(String username, PasswordChangeRequestDto requestDto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
        
        if (user.isDeleted()) {
            throw new CustomException(CommonErrorCode.USER_NOT_FOUND);
        }

        String currentPassword = requestDto.getCurrentPassword();
        String newPassword = requestDto.getNewPassword();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException(CommonErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호로 변경
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     */
    @Transactional
    public void deleteProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
        
        if (user.isDeleted()) {
            throw new CustomException(CommonErrorCode.USER_NOT_FOUND);
        }

        // 소프트 삭제 (삭제한 사용자 정보 기록)
        user.softDelete(username);
    }
}   
