package com.phonebid.app.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.phonebid.app.member.domain.Role;
import com.phonebid.app.common.Constants;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT에 관한 기능들을 가진 유틸리티 클래스입니다.
 */
@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    // Header KEY 값 (= cookie의 name 값)
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }
    
    /**
     * JWT 토큰 생성 (keepLoggedIn 파라미터 포함)
     * @param username 사용자명
     * @param role 사용자 역할
     * @param keepLoggedIn 로그인 상태 유지 여부 (true: 30일, false: 30분)
     * @return JWT 토큰 (Bearer 접두사 포함)
     */
    public String createToken(String username, Role role, boolean keepLoggedIn) {
        Date date = new Date();
        
        // keepLoggedIn 값에 따라 토큰 만료 시간 계산
        long tokenExpiryMillis = keepLoggedIn 
                ? Constants.Jwt.KEEP_LOGGED_IN_EXPIRY_MILLIS 
                : Constants.Jwt.DEFAULT_EXPIRY_MILLIS;

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim(AUTHORIZATION_KEY, role)
                        .setExpiration(new Date(date.getTime() + tokenExpiryMillis))
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }
    
    /**
     * JWT 토큰 생성 (기본 만료 시간: 30분)
     * @param username 사용자명
     * @param role 사용자 역할
     * @return JWT 토큰 (Bearer 접두사 포함)
     */
    public String createToken(String username, Role role) {
        return createToken(username, role, false);
    }
    
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 쿠키에서 JWT 토큰을 가져오는 메서드
     * @param request HTTP 요청 객체
     * @return JWT 토큰 (없으면 null)
     */
    public String getJwtFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (AUTHORIZATION_HEADER.equals(cookie.getName())) {
                    String tokenValue = cookie.getValue();
                    if (StringUtils.hasText(tokenValue)) {
                        return tokenValue;
                    }
                }
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }
 
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * Refresh Token 생성 (만료 시간: 2주)
     * @param username 사용자명
     * @return Refresh Token (Bearer 접두사 없이 순수 토큰만 반환)
     */
    public String createRefreshToken(String username) {
        Date date = new Date();
        
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(date.getTime() + Constants.Jwt.REFRESH_TOKEN_EXPIRY_MILLIS))
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    /**
     * Refresh Token 검증
     * @param token Refresh Token
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid Refresh Token signature, 유효하지 않는 Refresh Token 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired Refresh Token, 만료된 Refresh Token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported Refresh Token, 지원되지 않는 Refresh Token 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("Refresh Token claims is empty, 잘못된 Refresh Token 입니다.");
        }
        return false;
    }

    /**
     * 쿠키에서 Refresh Token을 가져오는 메서드
     * @param request HTTP 요청 객체
     * @return Refresh Token (없으면 null)
     */
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    String tokenValue = cookie.getValue();
                    if (StringUtils.hasText(tokenValue)) {
                        return tokenValue;
                    }
                }
            }
        }
        return null;
    }
}
