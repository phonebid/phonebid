package com.phonebid.app.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.RequestDto.LoginRequestDto;
import com.phonebid.app.member.dto.ResponseDto.LoginResponseDto;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.jwt.JwtUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private LoginRequestDto validLoginRequest;
    private LoginRequestDto invalidPasswordRequest;
    private LoginRequestDto nonExistentUserRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .role(Role.CONSUMER)
                .build();

        validLoginRequest = LoginRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        invalidPasswordRequest = LoginRequestDto.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        nonExistentUserRequest = LoginRequestDto.builder()
                .username("nonexistent")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("올바른 아이디와 비밀번호로 로그인 성공")
    void loginSuccess() {
        // given
        String expectedToken = "Bearer test.jwt.token";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.createToken("testuser", Role.CONSUMER)).thenReturn(expectedToken);

        // when
        LoginResponseDto result = userService.login(validLoginRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getNickname()).isEqualTo("테스트닉네임");
        assertThat(result.getRole()).isEqualTo("CONSUMER");
        assertThat(result.getAccessToken()).isEqualTo(expectedToken);
        assertThat(result.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시도 시 예외 발생")
    void loginWithNonExistentUser() {
        // given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(nonExistentUserRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 예외 발생")
    void loginWithWrongPassword() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(invalidPasswordRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("빈 사용자명으로 로그인 시도 시 예외 발생")
    void loginWithEmptyUsername() {
        // given
        LoginRequestDto emptyUsernameRequest = LoginRequestDto.builder()
                .username("")
                .password("password123")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.login(emptyUsernameRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_CREDENTIALS);
    }
} 