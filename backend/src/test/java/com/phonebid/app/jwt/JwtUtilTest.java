package com.phonebid.app.jwt;

import com.phonebid.app.user.domain.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
    "service.jwt.secret-key=testSecretKeyForJwtUtilTestingPurposesOnly123456789"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_USERNAME = "testuser";
    private static final Role TEST_ROLE = Role.CONSUMER;

    @BeforeEach
    void setUp() {
        // JwtUtil의 @PostConstruct가 실행되도록 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void createToken_ShouldCreateValidToken() {
        // when
        String token = jwtUtil.createToken(TEST_USERNAME, TEST_ROLE);

        // then
        assertThat(token).isNotNull();
        assertThat(token).startsWith("Bearer ");
        assertThat(token.length()).isGreaterThan(50);
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 테스트")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // given
        String token = jwtUtil.createToken(TEST_USERNAME, TEST_ROLE);
        String jwtToken = token.substring(7); // "Bearer " 제거

        // when
        boolean isValid = jwtUtil.validateToken(jwtToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰 검증 테스트")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자 정보 추출 테스트")
    void getUserInfoFromToken_WithValidToken_ShouldReturnClaims() {
        // given
        String token = jwtUtil.createToken(TEST_USERNAME, TEST_ROLE);
        String jwtToken = token.substring(7); // "Bearer " 제거

        // when
        Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(TEST_USERNAME);
        assertThat(claims.get(JwtUtil.AUTHORIZATION_KEY)).isEqualTo(TEST_ROLE.toString());
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰에서 사용자 정보 추출 시 예외 발생 테스트")
    void getUserInfoFromToken_WithInvalidToken_ShouldThrowException() {
        // given
        String invalidToken = "invalid.jwt.token";

        // then
        assertThatThrownBy(() -> jwtUtil.getUserInfoFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("HTTP 헤더에서 JWT 토큰 추출 테스트 - Bearer 토큰")
    void getJwtFromHeader_WithBearerToken_ShouldReturnToken() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String expectedToken = "test.jwt.token";
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, "Bearer " + expectedToken);

        // when
        String extractedToken = jwtUtil.getJwtFromHeader(request);

        // then
        assertThat(extractedToken).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("HTTP 헤더에서 JWT 토큰 추출 테스트 - Bearer 접두사 없음")
    void getJwtFromHeader_WithoutBearerPrefix_ShouldReturnNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, "test.jwt.token");

        // when
        String extractedToken = jwtUtil.getJwtFromHeader(request);

        // then
        assertThat(extractedToken).isNull();
    }

    @Test
    @DisplayName("HTTP 헤더에서 JWT 토큰 추출 테스트 - 헤더 없음")
    void getJwtFromHeader_WithoutHeader_ShouldReturnNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        String extractedToken = jwtUtil.getJwtFromHeader(request);

        // then
        assertThat(extractedToken).isNull();
    }

    @Test
    @DisplayName("HTTP 헤더에서 JWT 토큰 추출 테스트 - 빈 헤더")
    void getJwtFromHeader_WithEmptyHeader_ShouldReturnNull() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, "");

        // when
        String extractedToken = jwtUtil.getJwtFromHeader(request);

        // then
        assertThat(extractedToken).isNull();
    }

    @Test
    @DisplayName("다양한 역할로 JWT 토큰 생성 및 검증 테스트")
    void createToken_WithDifferentRoles_ShouldCreateValidTokens() {
        // given
        Role[] roles = {Role.CONSUMER, Role.SELLER, Role.ADMIN};

        for (Role role : roles) {
            // when
            String token = jwtUtil.createToken(TEST_USERNAME, role);
            String jwtToken = token.substring(7);
            Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);

            // then
            assertThat(claims.getSubject()).isEqualTo(TEST_USERNAME);
            assertThat(claims.get(JwtUtil.AUTHORIZATION_KEY)).isEqualTo(role.toString());
            assertThat(jwtUtil.validateToken(jwtToken)).isTrue();
        }
    }
} 