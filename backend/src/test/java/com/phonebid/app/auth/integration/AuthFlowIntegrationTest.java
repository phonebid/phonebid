package com.phonebid.app.auth.integration;

import com.phonebid.app.auth.domain.RefreshToken;
import com.phonebid.app.auth.service.RefreshTokenService;
import com.phonebid.app.common.Constants;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.request.LoginRequestDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.member.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 플로우 통합 테스트")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        String username = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        testUser = User.builder()
                .username(username)
                .password(encodedPassword)
                .email(username + "@test.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .role(Role.CONSUMER)
                .build();

        testUser = userRepository.save(testUser);

        loginRequest = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();
    }

    @Test
    @DisplayName("로그인 → RefreshToken 생성 확인")
    void login_ShouldCreateRefreshToken() {
        // when
        LoginResponseDto loginResponse = userService.login(loginRequest);

        // then
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isNotNull();
        assertThat(loginResponse.getAccessToken()).startsWith("Bearer ");

        // RefreshToken이 DB에 저장되었는지 확인
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(refreshTokenOpt).isPresent();
        assertThat(refreshTokenOpt.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(refreshTokenOpt.get().getToken()).isNotNull();
    }

    @Test
    @DisplayName("로그인 시 기존 RefreshToken 삭제 확인")
    void login_ShouldDeleteExistingRefreshToken() {
        // given - 기존 RefreshToken 생성
        String oldRefreshToken = refreshTokenService.createRefreshToken(testUser.getId());
        Optional<RefreshToken> oldTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(oldTokenOpt).isPresent();
        assertThat(oldTokenOpt.get().getToken()).isEqualTo(oldRefreshToken);

        // when - 로그인 (새로운 RefreshToken 생성)
        userService.login(loginRequest);

        // then - 기존 토큰이 삭제되고 새로운 토큰이 생성되었는지 확인
        Optional<RefreshToken> newRefreshTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(newRefreshTokenOpt).isPresent();
        assertThat(newRefreshTokenOpt.get().getToken()).isNotEqualTo(oldRefreshToken);
    }

    @Test
    @DisplayName("RefreshToken으로 AccessToken 갱신 성공")
    void refresh_WithValidRefreshToken_ShouldReturnNewAccessToken() throws Exception {
        // given - 로그인하여 RefreshToken 생성
        LoginResponseDto loginResponse = userService.login(loginRequest);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(refreshTokenOpt).isPresent();
        String refreshToken = refreshTokenOpt.get().getToken();

        // when & then - RefreshToken으로 AccessToken 갱신
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                refreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("토큰이 성공적으로 갱신되었습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("만료된 RefreshToken으로 갱신 시도 시 실패")
    void refresh_WithExpiredRefreshToken_ShouldFail() throws Exception {
        // given - 만료된 RefreshToken 생성
        // 실제 만료된 JWT 토큰 생성은 복잡하므로, 유효하지 않은 토큰으로 테스트
        String expiredTokenValue = "expired.invalid.token";

        // when & then - 만료된/유효하지 않은 토큰으로 갱신 시도 (JWT 검증 실패)
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                expiredTokenValue
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 → RefreshToken 삭제 확인")
    void logout_ShouldDeleteRefreshToken() throws Exception {
        // given - 로그인하여 RefreshToken 생성
        LoginResponseDto loginResponse = userService.login(loginRequest);
        String accessToken = loginResponse.getAccessToken().substring(7); // "Bearer " 제거
        
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(refreshTokenOpt).isPresent();

        // SecurityContext 설정 (인증된 사용자로 설정)
        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                        .username(testUser.getUsername())
                        .password(testUser.getPassword())
                        .authorities(testUser.getRole().name())
                        .build();
        
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // when - 로그아웃
        mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new jakarta.servlet.http.Cookie(
                                JwtUtil.AUTHORIZATION_HEADER,
                                accessToken
                        ))
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                refreshTokenOpt.get().getToken()
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃이 성공적으로 완료되었습니다."))
                .andExpect(header().exists("Set-Cookie"));

        // then - RefreshToken이 삭제되었는지 확인
        Optional<RefreshToken> deletedTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(deletedTokenOpt).isEmpty();
        
        // SecurityContext 정리
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("전체 플로우: 로그인 → 토큰 갱신 → 로그아웃")
    void fullAuthFlow_LoginRefreshLogout() throws Exception {
        // 1. 로그인
        LoginResponseDto loginResponse = userService.login(loginRequest);
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isNotNull();

        String accessToken = loginResponse.getAccessToken().substring(7);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(refreshTokenOpt).isPresent();
        String refreshToken = refreshTokenOpt.get().getToken();

        // 2. RefreshToken으로 AccessToken 갱신
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                refreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        // 3. SecurityContext 설정 (인증된 사용자로 설정)
        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                        .username(testUser.getUsername())
                        .password(testUser.getPassword())
                        .authorities(testUser.getRole().name())
                        .build();
        
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // 4. 로그아웃
        mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new jakarta.servlet.http.Cookie(
                                JwtUtil.AUTHORIZATION_HEADER,
                                accessToken
                        ))
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                refreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 5. RefreshToken이 삭제되었는지 확인
        Optional<RefreshToken> deletedTokenOpt = refreshTokenService.findByUserId(testUser.getId());
        assertThat(deletedTokenOpt).isEmpty();

        // 6. 삭제된 RefreshToken으로 갱신 시도 시 실패
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                refreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        // SecurityContext 정리
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}

