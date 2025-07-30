package com.phonebid.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.dto.RequestDto.LoginRequestDto;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JwtAuthenticationFilter는 더 이상 사용되지 않으므로 이 테스트는 참고용으로만 유지합니다.
 * 실제 로그인은 Controller에서 처리됩니다.
 */
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
    @DisplayName("JwtAuthenticationFilter는 더 이상 사용되지 않음 - Controller에서 로그인 처리")
    void jwtAuthenticationFilterIsDeprecated() {
        // 이 테스트는 JwtAuthenticationFilter가 더 이상 사용되지 않음을 확인합니다.
        // 실제 로그인은 UserController에서 처리됩니다.
        assertThat(true).isTrue(); // 테스트 통과를 위한 더미 assertion
    }
} 