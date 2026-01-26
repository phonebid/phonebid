package com.phonebid.app.auth.service;

import com.phonebid.app.auth.domain.RefreshToken;
import com.phonebid.app.auth.repository.RefreshTokenRepository;
import com.phonebid.app.auth.util.TokenHashUtil;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DbRefreshTokenService 테스트")
class DbRefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenHashUtil tokenHashUtil;

    @InjectMocks
    private DbRefreshTokenService dbRefreshTokenService;

    private User testUser;
    private UUID testUserId;
    private RefreshToken existingRefreshToken;
    private String testToken;
    private String hashedToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testToken = "test.refresh.token";
        hashedToken = "hashed.test.refresh.token";

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .role(Role.CONSUMER)
                .build();
        
        // Reflection을 사용하여 id 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user id", e);
        }

        existingRefreshToken = RefreshToken.builder()
                .user(testUser)
                .token("hashed.old.refresh.token")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("RefreshToken 생성 및 저장 성공")
    void createRefreshToken_ShouldCreateAndSaveToken() {
        // given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtUtil.createRefreshToken(testUser.getUsername())).thenReturn(testToken);
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            return token;
        });

        // when
        String result = dbRefreshTokenService.createRefreshToken(testUserId);

        // then
        assertThat(result).isEqualTo(testToken);
        verify(refreshTokenRepository).deleteByUserId(testUserId);
        verify(userRepository).findById(testUserId);
        verify(jwtUtil).createRefreshToken(testUser.getUsername());
        verify(tokenHashUtil).hashToken(testToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RefreshToken 생성 시 기존 토큰 삭제 확인")
    void createRefreshToken_ShouldDeleteExistingToken() {
        // given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtUtil.createRefreshToken(testUser.getUsername())).thenReturn(testToken);
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        dbRefreshTokenService.createRefreshToken(testUserId);

        // then
        verify(refreshTokenRepository).deleteByUserId(testUserId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 RefreshToken 생성 시 예외 발생")
    void createRefreshToken_WithNonExistentUser_ShouldThrowException() {
        // given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dbRefreshTokenService.createRefreshToken(testUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(testUserId);
        verify(refreshTokenRepository, never()).deleteByUserId(any(UUID.class));
        verify(jwtUtil, never()).createRefreshToken(anyString());
        verify(tokenHashUtil, never()).hashToken(anyString());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰으로 RefreshToken 조회 성공")
    void findByToken_ShouldReturnRefreshToken() {
        // given
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.of(existingRefreshToken));

        // when
        Optional<RefreshToken> result = dbRefreshTokenService.findByToken(testToken);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("hashed.old.refresh.token");
        verify(tokenHashUtil).hashToken(testToken);
        verify(refreshTokenRepository).findByToken(hashedToken);
    }

    @Test
    @DisplayName("존재하지 않는 토큰으로 조회 시 Optional.empty() 반환")
    void findByToken_WithNonExistentToken_ShouldReturnEmpty() {
        // given
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.empty());

        // when
        Optional<RefreshToken> result = dbRefreshTokenService.findByToken(testToken);

        // then
        assertThat(result).isEmpty();
        verify(tokenHashUtil).hashToken(testToken);
        verify(refreshTokenRepository).findByToken(hashedToken);
    }

    @Test
    @DisplayName("사용자 ID로 RefreshToken 조회 성공")
    void findByUserId_ShouldReturnRefreshToken() {
        // given
        when(refreshTokenRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(existingRefreshToken));

        // when
        Optional<RefreshToken> result = dbRefreshTokenService.findByUserId(testUserId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(testUserId);
        verify(refreshTokenRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 Optional.empty() 반환")
    void findByUserId_WithNonExistentUserId_ShouldReturnEmpty() {
        // given
        when(refreshTokenRepository.findByUserId(testUserId))
                .thenReturn(Optional.empty());

        // when
        Optional<RefreshToken> result = dbRefreshTokenService.findByUserId(testUserId);

        // then
        assertThat(result).isEmpty();
        verify(refreshTokenRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("사용자 ID로 RefreshToken 삭제 성공")
    void deleteByUserId_ShouldDeleteToken() {
        // given
        doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);

        // when
        dbRefreshTokenService.deleteByUserId(testUserId);

        // then
        verify(refreshTokenRepository).deleteByUserId(testUserId);
    }

    @Test
    @DisplayName("만료된 RefreshToken 삭제 성공")
    void deleteExpiredTokens_ShouldDeleteExpiredTokens() {
        // given
        doNothing().when(refreshTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // when
        dbRefreshTokenService.deleteExpiredTokens();

        // then
        verify(refreshTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("유효한 RefreshToken 검증 성공")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // given
        RefreshToken validToken = RefreshToken.builder()
                .user(testUser)
                .token(hashedToken)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(jwtUtil.validateRefreshToken(testToken)).thenReturn(true);
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.of(validToken));

        // when
        boolean result = dbRefreshTokenService.validateToken(testToken);

        // then
        assertThat(result).isTrue();
        verify(jwtUtil).validateRefreshToken(testToken);
        verify(tokenHashUtil).hashToken(testToken);
        verify(refreshTokenRepository).findByToken(hashedToken);
    }

    @Test
    @DisplayName("JWT 검증 실패 시 false 반환")
    void validateToken_WithInvalidJwt_ShouldReturnFalse() {
        // given
        when(jwtUtil.validateRefreshToken(testToken)).thenReturn(false);

        // when
        boolean result = dbRefreshTokenService.validateToken(testToken);

        // then
        assertThat(result).isFalse();
        verify(jwtUtil).validateRefreshToken(testToken);
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    @DisplayName("DB에 존재하지 않는 토큰 검증 시 false 반환")
    void validateToken_WithNonExistentToken_ShouldReturnFalse() {
        // given
        when(jwtUtil.validateRefreshToken(testToken)).thenReturn(true);
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.empty());

        // when
        boolean result = dbRefreshTokenService.validateToken(testToken);

        // then
        assertThat(result).isFalse();
        verify(jwtUtil).validateRefreshToken(testToken);
        verify(tokenHashUtil).hashToken(testToken);
        verify(refreshTokenRepository).findByToken(hashedToken);
    }

    @Test
    @DisplayName("만료된 RefreshToken 검증 시 false 반환")
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // given
        RefreshToken expiredToken = RefreshToken.builder()
                .user(testUser)
                .token(hashedToken)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(jwtUtil.validateRefreshToken(testToken)).thenReturn(true);
        when(tokenHashUtil.hashToken(testToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.of(expiredToken));

        // when
        boolean result = dbRefreshTokenService.validateToken(testToken);

        // then
        assertThat(result).isFalse();
        verify(jwtUtil).validateRefreshToken(testToken);
        verify(tokenHashUtil).hashToken(testToken);
        verify(refreshTokenRepository).findByToken(hashedToken);
    }
}

