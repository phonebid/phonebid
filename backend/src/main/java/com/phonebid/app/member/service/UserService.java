package com.phonebid.app.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.dto.RequestDto.SignupRequestDto;
import com.phonebid.app.member.dto.RequestDto.LoginRequestDto;
import com.phonebid.app.member.dto.ResponseDto.LoginResponseDto;
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
}   
