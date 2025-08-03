package com.phonebid.app.member.service;

import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.RequestDto.SignupRequestDto;
import com.phonebid.app.member.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.exception.CommonErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignupRequestDto validSignupRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // 유효한 회원가입 요청 데이터
        validSignupRequest = SignupRequestDto.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .build();

        // 저장될 사용자 객체
        savedUser = User.builder()
                .username("testuser")
                .password("encodedPassword123")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .build();
    }

    @Test
    @DisplayName("정상적인 회원가입 성공")
    void signupSuccess() {
        // given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.signup(validSignupRequest);

        // then
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).findByNickname("테스트닉네임");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 username으로 회원가입 시 예외 발생")
    void signupWithDuplicateUsername() {
        // given
        User existingUser = User.builder()
                .username("testuser")
                .email("existing@example.com")
                .name("기존 사용자")
                .nickname("기존닉네임")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.signup(validSignupRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.DUPLICATE_USERNAME);

        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByNickname(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 email로 회원가입 시 예외 발생")
    void signupWithDuplicateEmail() {
        // given
        User existingUser = User.builder()
                .username("existinguser")
                .email("test@example.com")
                .name("기존 사용자")
                .nickname("기존닉네임")
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.signup(validSignupRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.DUPLICATE_EMAIL);

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).findByNickname(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 nickname으로 회원가입 시 예외 발생")
    void signupWithDuplicateNickname() {
        // given
        User existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .name("기존 사용자")
                .nickname("테스트닉네임")
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByNickname("테스트닉네임")).thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.signup(validSignupRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.DUPLICATE_NICKNAME);

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).findByNickname("테스트닉네임");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 올바르게 인코딩되어 저장")
    void passwordIsEncoded() {
        // given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.signup(validSignupRequest);

        // then
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user -> 
            user.getPassword().equals("encodedPassword123")
        ));
    }

    @Test
    @DisplayName("사용자 정보 올바르게 저장")
    void userInformationIsSavedCorrectly() {
        // given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.signup(validSignupRequest);

        // then
        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("testuser") &&
            user.getEmail().equals("test@example.com") &&
            user.getName().equals("테스트 사용자") &&
            user.getNickname().equals("테스트닉네임")
        ));
    }
} 