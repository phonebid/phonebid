package com.phonebid.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.user.domain.Role;
import com.phonebid.app.user.domain.User;
import com.phonebid.app.user.dto.RequestDto.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.FilterChain;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰이 헤더에 추가되는지 테스트")
    void successfulAuthentication_ShouldAddJwtTokenToHeader() throws Exception {
        // given
        String username = "testuser";
        String password = "testpass";
        Role role = Role.CONSUMER;
        
        // User 객체 생성
        User user = User.builder()
                .username(username)
                .password(password)
                .email("test@test.com")
                .name("Test User")
                .nickname("tester")
                .role(role)
                .build();
        
        // UserDetailsImpl 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(user, username);
        
        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        // LoginRequestDto 생성 및 JSON 변환
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername(username);
        loginRequestDto.setPassword(password);
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);
        
        // Mock 설정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        
        String expectedToken = "Bearer test.jwt.token";
        when(jwtUtil.createToken(username, role)).thenReturn(expectedToken);
        
        // request body 설정
        request.setContent(requestBody.getBytes());
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/user/login");
        
        // when
        jwtAuthenticationFilter.attemptAuthentication(request, response);
        jwtAuthenticationFilter.successfulAuthentication(request, response, filterChain, authentication);
        
        // then
        verify(jwtUtil).createToken(username, role);
        assertThat(response.getHeader("Authorization")).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("로그인 시도 시 올바른 인증 토큰이 생성되는지 테스트")
    void attemptAuthentication_ShouldCreateCorrectAuthenticationToken() throws Exception {
        // given
        String username = "testuser";
        String password = "testpass";
        
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername(username);
        loginRequestDto.setPassword(password);
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);
        
        User user = User.builder()
                .username(username)
                .password(password)
                .email("test@test.com")
                .name("Test User")
                .nickname("tester")
                .role(Role.CONSUMER)
                .build();
        
        UserDetailsImpl userDetails = new UserDetailsImpl(user, username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        
        // request body 설정
        request.setContent(requestBody.getBytes());
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/user/login");
        
        // when
        Authentication result = jwtAuthenticationFilter.attemptAuthentication(request, response);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(userDetails);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("다양한 역할로 로그인 시 올바른 JWT 토큰이 생성되는지 테스트")
    void successfulAuthentication_WithDifferentRoles_ShouldCreateCorrectTokens() throws Exception {
        // given
        Role[] roles = {Role.CONSUMER, Role.SELLER, Role.ADMIN};
        String username = "testuser";
        
        for (Role role : roles) {
            User user = User.builder()
                    .username(username)
                    .password("testpass")
                    .email("test@test.com")
                    .name("Test User")
                    .nickname("tester")
                    .role(role)
                    .build();
            
            UserDetailsImpl userDetails = new UserDetailsImpl(user, username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            String expectedToken = "Bearer " + role.name().toLowerCase() + ".token";
            when(jwtUtil.createToken(username, role)).thenReturn(expectedToken);
            
            // when
            jwtAuthenticationFilter.successfulAuthentication(request, response, filterChain, authentication);
            
            // then
            verify(jwtUtil).createToken(username, role);
            assertThat(response.getHeader("Authorization")).isEqualTo(expectedToken);
            
            // 다음 테스트를 위해 response 초기화
            response = new MockHttpServletResponse();
        }
    }

    @Test
    @DisplayName("잘못된 JSON 요청 시 예외가 발생하는지 테스트")
    void attemptAuthentication_WithInvalidJson_ShouldThrowException() {
        // given
        String invalidJson = "{ invalid json }";
        request.setContent(invalidJson.getBytes());
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/user/login");
        
        // when & then
        assertThatThrownBy(() -> jwtAuthenticationFilter.attemptAuthentication(request, response))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("인증 실패 시 예외가 발생하는지 테스트")
    void attemptAuthentication_WithInvalidCredentials_ShouldThrowException() throws Exception {
        // given
        String username = "testuser";
        String password = "wrongpass";
        
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername(username);
        loginRequestDto.setPassword(password);
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {});
        
        request.setContent(requestBody.getBytes());
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/user/login");
        
        // when & then
        assertThatThrownBy(() -> jwtAuthenticationFilter.attemptAuthentication(request, response))
                .isInstanceOf(AuthenticationException.class);
    }
} 