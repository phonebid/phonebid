package com.phonebid.app.security;

import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

import com.phonebid.app.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 들어온 JWT를 검증 및 인가하는 클래스입니다.
 * */
@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;
    
    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 공개 경로는 JWT 토큰 검증을 건너뜁니다.
     * 로그인, 회원가입, OAuth 등 인증 없이 접근 가능한 엔드포인트
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/users/login")
            || path.equals("/api/v1/users/signup")
            || path.startsWith("/api/v1/auth/kakao")
            || path.startsWith("/api/v1/auth/naver");
            // || path.startsWith("/api/v1/payments/portone");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 토큰 값 가져오기 (우선순위 1)
        String tokenValue = jwtUtil.getJwtFromHeader(req);
        
        // 헤더에 토큰이 없으면 쿠키에서 토큰 가져오기 (우선순위 2)
        if (!StringUtils.hasText(tokenValue)) {
            tokenValue = jwtUtil.getJwtFromCookie(req);
        }

        if (StringUtils.hasText(tokenValue)) {

            // 토큰 검증
            if (!jwtUtil.validateToken(tokenValue)) {
                log.warn("JWT 토큰 검증 실패: tokenFingerprint={}", tokenFingerprint(tokenValue));
                sendUnauthorizedResponse(res, "유효하지 않은 토큰입니다.");
                return;
            }

            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
            
            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.warn("사용자 인증 처리 중 오류 발생", e);
                // 실패 시 잔존 인증 정보 제거
                SecurityContextHolder.clearContext();
                sendUnauthorizedResponse(res, "사용자 인증에 실패했습니다.");
                return;
            }
        }
        filterChain.doFilter(req, res);
    }

    /**
     * 401 Unauthorized 응답을 클라이언트에 전송
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "UNAUTHORIZED");
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    // 인증 처리
    private void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);  // 사용자 정보 가져오기
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * 토큰의 안전한 해시값을 생성하여 로깅에 사용하는 메서드
     */
    private String tokenFingerprint(String token) {
        if (token == null || token.isBlank()) return "blank";
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // Java 17 HexFormat 사용
            String hex = java.util.HexFormat.of().formatHex(digest);
            // 앞 12자리만 사용
            return hex.substring(0, 12);
        } catch (Exception e) {
            return "hash_error";
        }
    }
}
