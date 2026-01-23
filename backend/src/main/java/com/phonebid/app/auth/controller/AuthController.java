package com.phonebid.app.auth.controller;

import com.phonebid.app.auth.service.RefreshTokenService;
import com.phonebid.app.common.Constants;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 인증 관련 컨트롤러
 * Refresh Token 갱신 등을 처리
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    /**
     * Refresh Token으로 Access Token 갱신
     * @param request HTTP 요청 (쿠키에서 Refresh Token 추출)
     * @return 새로운 Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(HttpServletRequest request) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        
        if (refreshToken == null) {
            throw new CustomException(CommonErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // Refresh Token 검증
        if (!refreshTokenService.validateToken(refreshToken)) {
            throw new CustomException(CommonErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token에서 사용자 정보 추출
        String username = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));

        // 새로운 Access Token 생성 (기본 만료 시간: 1시간)
        String newAccessToken = jwtUtil.createToken(user.getUsername(), user.getRole(), false);
        
        // Bearer 접두사 제거
        String accessTokenValue = newAccessToken.startsWith(JwtUtil.BEARER_PREFIX) 
            ? newAccessToken.substring(JwtUtil.BEARER_PREFIX.length()) 
            : newAccessToken;

        // Access Token을 쿠키에 저장
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        ResponseCookie accessTokenCookie = ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, accessTokenValue)
                .path("/")
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .maxAge(Constants.Jwt.DEFAULT_EXPIRY) // 1시간
                .build();

        log.info("Access Token 갱신 완료: username={}", username);

        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .body(ApiResponse.success(HttpStatus.OK, "토큰이 성공적으로 갱신되었습니다.", newAccessToken));
    }
}

