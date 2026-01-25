package com.phonebid.app.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.auth.service.RefreshTokenService;
import com.phonebid.app.common.Constants;
import com.phonebid.app.common.exception.GlobalExceptionHandler;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Environment environment;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private String testRefreshToken;
    private String testAccessToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .role(Role.CONSUMER)
                .build();

        testRefreshToken = "test.refresh.token";
        testAccessToken = "Bearer test.access.token";

        // lenient()를 사용하여 일부 테스트에서 사용되지 않아도 되는 stubbing 허용
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 Access Token 갱신 성공")
    void refresh_WithValidRefreshToken_ShouldReturnNewAccessToken() throws Exception {
        // given
        Claims claims = Jwts.claims().setSubject("testuser");
        
        when(jwtUtil.getRefreshTokenFromCookie(any())).thenReturn(testRefreshToken);
        when(refreshTokenService.validateToken(testRefreshToken)).thenReturn(true);
        when(jwtUtil.getUserInfoFromToken(testRefreshToken)).thenReturn(claims);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.createToken(testUser.getUsername(), testUser.getRole(), false))
                .thenReturn(testAccessToken);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                testRefreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("토큰이 성공적으로 갱신되었습니다."))
                .andExpect(jsonPath("$.data").value(testAccessToken))
                .andExpect(header().exists("Set-Cookie"));

        verify(jwtUtil).getRefreshTokenFromCookie(any());
        verify(refreshTokenService).validateToken(testRefreshToken);
        verify(jwtUtil).getUserInfoFromToken(testRefreshToken);
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtil).createToken(testUser.getUsername(), testUser.getRole(), false);
    }

    @Test
    @DisplayName("Refresh Token이 없는 경우 예외 발생")
    void refresh_WithoutRefreshToken_ShouldThrowException() throws Exception {
        // given
        when(jwtUtil.getRefreshTokenFromCookie(any())).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(CommonErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));

        verify(jwtUtil).getRefreshTokenFromCookie(any());
        verify(refreshTokenService, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtil, never()).createToken(anyString(), any(Role.class), anyBoolean());
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token 처리")
    void refresh_WithInvalidRefreshToken_ShouldThrowException() throws Exception {
        // given
        when(jwtUtil.getRefreshTokenFromCookie(any())).thenReturn(testRefreshToken);
        when(refreshTokenService.validateToken(testRefreshToken)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                testRefreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_REFRESH_TOKEN.getMessage()));

        verify(jwtUtil).getRefreshTokenFromCookie(any());
        verify(refreshTokenService).validateToken(testRefreshToken);
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtil, never()).createToken(anyString(), any(Role.class), anyBoolean());
    }

    @Test
    @DisplayName("만료된 Refresh Token 처리")
    void refresh_WithExpiredRefreshToken_ShouldThrowException() throws Exception {
        // given
        when(jwtUtil.getRefreshTokenFromCookie(any())).thenReturn(testRefreshToken);
        when(refreshTokenService.validateToken(testRefreshToken)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                testRefreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_REFRESH_TOKEN.getMessage()));

        verify(jwtUtil).getRefreshTokenFromCookie(any());
        verify(refreshTokenService).validateToken(testRefreshToken);
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtil, never()).createToken(anyString(), any(Role.class), anyBoolean());
    }

    @Test
    @DisplayName("Refresh Token에 해당하는 사용자가 없는 경우 예외 발생")
    void refresh_WithNonExistentUser_ShouldThrowException() throws Exception {
        // given
        Claims claims = Jwts.claims().setSubject("nonexistent");
        
        when(jwtUtil.getRefreshTokenFromCookie(any())).thenReturn(testRefreshToken);
        when(refreshTokenService.validateToken(testRefreshToken)).thenReturn(true);
        when(jwtUtil.getUserInfoFromToken(testRefreshToken)).thenReturn(claims);
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME,
                                testRefreshToken
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(CommonErrorCode.USER_NOT_FOUND.getMessage()));

        verify(jwtUtil).getRefreshTokenFromCookie(any());
        verify(refreshTokenService).validateToken(testRefreshToken);
        verify(jwtUtil).getUserInfoFromToken(testRefreshToken);
        verify(userRepository).findByUsername("nonexistent");
        verify(jwtUtil, never()).createToken(anyString(), any(Role.class), anyBoolean());
    }
}

