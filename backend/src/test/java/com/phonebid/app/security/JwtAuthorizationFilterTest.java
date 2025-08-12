package com.phonebid.app.security;

import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.Provider;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 인가 필터 테스트")
class JwtAuthorizationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthorizationFilter jwtAuthorizationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String TEST_USERNAME = "testuser";
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;

    @BeforeEach
    void setUp() {
        jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증이 성공적으로 설정되는지 테스트")
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", BEARER_TOKEN);
        
        User testUser = createTestUser(TEST_USERNAME, Role.CONSUMER);
        UserDetails userDetails = new UserDetailsImpl(testUser, TEST_USERNAME);
        
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserInfoFromToken(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).getJwtFromHeader(request);
        verify(jwtUtil).validateToken(VALID_TOKEN);
        verify(jwtUtil).getUserInfoFromToken(VALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(TEST_USERNAME);
        verify(filterChain).doFilter(request, response);
        
        // SecurityContext에 인증이 설정되었는지 확인
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().getPrincipal()).isEqualTo(userDetails);
        assertThat(context.getAuthentication().getAuthorities()).isEqualTo(userDetails.getAuthorities());
    }

    @Test
    @DisplayName("다양한 역할의 사용자로 인증이 성공적으로 설정되는지 테스트")
    void doFilterInternal_WithDifferentRoles_ShouldSetAuthentication() throws Exception {
        // given
        Role[] roles = {Role.CONSUMER, Role.SELLER, Role.ADMIN};
        
        for (Role role : roles) {
            // SecurityContext 초기화
            SecurityContextHolder.clearContext();
            
            request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + role.name().toLowerCase() + ".token");
            
            User testUser = createTestUser(TEST_USERNAME, role);
            UserDetails userDetails = new UserDetailsImpl(testUser, TEST_USERNAME);
            
            when(jwtUtil.getJwtFromHeader(request)).thenReturn(role.name().toLowerCase() + ".token");
            when(jwtUtil.validateToken(anyString())).thenReturn(true);
            when(jwtUtil.getUserInfoFromToken(anyString())).thenReturn(claims);
            when(claims.getSubject()).thenReturn(TEST_USERNAME);
            when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

            // when
            jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

            // then
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNotNull();
            assertThat(context.getAuthentication().getPrincipal()).isEqualTo(userDetails);
            assertThat(context.getAuthentication().getAuthorities().size()).isEqualTo(userDetails.getAuthorities().size());
        }
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰으로 401 응답이 반환되는지 테스트")
    void doFilterInternal_WithInvalidToken_ShouldReturn401() throws Exception {
        // given
        request.addHeader("Authorization", "Bearer " + INVALID_TOKEN);
        
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(INVALID_TOKEN);
        when(jwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).getJwtFromHeader(request);
        verify(jwtUtil).validateToken(INVALID_TOKEN);
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        // 토큰 검증 실패 시 early return되므로 filterChain이 호출되지 않음
        verify(filterChain, never()).doFilter(request, response);
        
        // 401 상태 코드 확인
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).contains("application/json");
        
        // 응답 본문 확인
        String responseBody = response.getContentAsString();
        assertThat(responseBody).contains("유효하지 않은 토큰입니다.");
        assertThat(responseBody).contains("UNAUTHORIZED");
        
        // SecurityContext에 인증이 설정되지 않았는지 확인
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }

    @Test
    @DisplayName("토큰이 없는 경우 인증이 설정되지 않는지 테스트")
    void doFilterInternal_WithoutToken_ShouldNotSetAuthentication() throws Exception {
        // given
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(null);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).getJwtFromHeader(request);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // SecurityContext에 인증이 설정되지 않았는지 확인
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }

    @Test
    @DisplayName("빈 토큰으로 인증이 설정되지 않는지 테스트")
    void doFilterInternal_WithEmptyToken_ShouldNotSetAuthentication() throws Exception {
        // given
        when(jwtUtil.getJwtFromHeader(request)).thenReturn("");

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).getJwtFromHeader(request);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // SecurityContext에 인증이 설정되지 않았는지 확인
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer 접두사가 없는 토큰은 처리되지 않는지 테스트")
    void doFilterInternal_WithoutBearerPrefix_ShouldNotProcessToken() throws Exception {
        // given
        request.addHeader("Authorization", VALID_TOKEN); // Bearer 접두사 없음
        
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(null);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).getJwtFromHeader(request);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).getUserInfoFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // SecurityContext에 인증이 설정되지 않았는지 확인
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }

    @Test
    @DisplayName("필터 체인이 모든 경우에 계속 실행되는지 테스트")
    void doFilterInternal_ShouldAlwaysContinueFilterChain() throws Exception {
        // given
        // 토큰이 없는 경우 - filterChain이 호출되어야 함
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(null);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        
        // 유효한 토큰의 경우 - filterChain이 호출되어야 함
        reset(filterChain);
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER_TOKEN);
        
        User testUser = createTestUser(TEST_USERNAME, Role.CONSUMER);
        UserDetails userDetails = new UserDetailsImpl(testUser, TEST_USERNAME);
        
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserInfoFromToken(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        
        // 빈 토큰의 경우 - filterChain이 호출되어야 함
        reset(filterChain);
        request = new MockHttpServletRequest();
        when(jwtUtil.getJwtFromHeader(request)).thenReturn("");

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("SecurityContext가 올바르게 설정되고 정리되는지 테스트")
    void doFilterInternal_ShouldProperlyManageSecurityContext() throws Exception {
        // given
        request.addHeader("Authorization", BEARER_TOKEN);
        
        User testUser = createTestUser(TEST_USERNAME, Role.ADMIN);
        UserDetails userDetails = new UserDetailsImpl(testUser, TEST_USERNAME);
        
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserInfoFromToken(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().getPrincipal()).isEqualTo(userDetails);
        assertThat(context.getAuthentication().getAuthorities()).isEqualTo(userDetails.getAuthorities());
        
        // 인증 객체의 타입과 속성 확인
        assertThat(context.getAuthentication().getClass().getSimpleName())
                .isEqualTo("UsernamePasswordAuthenticationToken");
        assertThat(context.getAuthentication().isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 401 응답이 반환되는지 테스트")
    void doFilterInternal_WithUserNotFound_ShouldReturn401() throws Exception {
        // given
        request.addHeader("Authorization", BEARER_TOKEN);
        
        when(jwtUtil.getJwtFromHeader(request)).thenReturn(VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserInfoFromToken(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME))
                .thenThrow(new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtUtil).getJwtFromHeader(request);
        verify(jwtUtil).validateToken(VALID_TOKEN);
        verify(jwtUtil).getUserInfoFromToken(VALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(TEST_USERNAME);
        // 예외 발생 시 early return되므로 filterChain이 호출되지 않음
        verify(filterChain, never()).doFilter(request, response);
        
        // 401 상태 코드 확인
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).contains("application/json");
        
        // 응답 본문 확인
        String responseBody = response.getContentAsString();
        assertThat(responseBody).contains("사용자 인증에 실패했습니다.");
        assertThat(responseBody).contains("UNAUTHORIZED");
        
        // SecurityContext에 인증이 설정되지 않았는지 확인
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }

    private User createTestUser(String username, Role role) {
        return User.builder()
                .username(username)
                .password("testpass")
                .email("test@test.com")
                .name("Test User")
                .nickname("tester")
                .role(role)
                .provider(Provider.KAKAO)
                .providerId("kakao123")
                .build();
    }
} 