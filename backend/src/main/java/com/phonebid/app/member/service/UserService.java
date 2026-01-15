package com.phonebid.app.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.dto.request.SignupRequestDto;
import com.phonebid.app.member.dto.request.LoginRequestDto;
import com.phonebid.app.member.dto.request.PasswordChangeRequestDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
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
        boolean keepLoggedIn = Boolean.TRUE.equals(requestDto.getKeepLoggedIn());

        // 사용자 존재 확인
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(CommonErrorCode.INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성 (keepLoggedIn 값에 따라 만료 시간 결정)
        String token = jwtUtil.createToken(user.getUsername(), user.getRole(), keepLoggedIn);
        
        // LoginResponseDto 생성 및 반환
        return LoginResponseDto.of(
            token, 
            user.getUsername(), 
            user.getNickname(), 
            user.getRole().name()
        );
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(String username, PasswordChangeRequestDto requestDto) {
        User user = loadActiveUser(username);

        String currentPassword = requestDto.getCurrentPassword();
        String newPassword = requestDto.getNewPassword();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException(CommonErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException(CommonErrorCode.SAME_PASSWORD);
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
        User user = loadActiveUser(username);

        // 소프트 삭제 (삭제한 사용자 정보 기록)
        user.softDelete(username);
    }

    /**
     * 활성 사용자 조회 헬퍼 메서드
     * 삭제되지 않은 사용자만 조회하며, 없으면 예외 발생
     */
    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
            .filter(user -> !user.isDeleted())
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }
}   
