package com.phonebid.app.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.exception.KakaoErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.dto.ResponseDto.LoginResponseDto;
import com.phonebid.app.member.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;

/**
 * 카카오 OAuth2 인증 컨트롤러
 * 카카오 소셜 로그인 관련 엔드포인트를 처리하는 클래스
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
public class KakaoController {
    
    private final KakaoService kakaoService;
    private final Environment environment;
    
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    /**
     * 카카오 로그인 콜백 처리 (카카오에서 직접 호출)
     * 카카오에서 인가 코드를 받아 JWT 토큰을 생성하고 쿠키에 저장
     * @param code 카카오 인가 코드
     * @param response HTTP 응답 객체
     */
    @GetMapping("/callback")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws Exception {
        try {
            log.info("카카오 로그인 콜백 처리 시작: code={}", code);
            
            // 인가 코드로 로그인 처리 및 JWT 토큰 생성
            LoginResponseDto loginResponse = kakaoService.kakaoLogin(code);
            String token = loginResponse.getAccessToken();
            
            log.info("카카오 로그인 성공: username={}", loginResponse.getUsername());
            
            // JWT 토큰을 보안 강화된 쿠키에 저장 (Bearer 접두사 제거)
            boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
            
            ResponseCookie cookie = ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, token.substring(7))
                    .path("/")
                    .httpOnly(true) // XSS 공격 방지
                    .secure(isProduction) // 프로덕션에서만 HTTPS 필수
                    .sameSite("Strict") // CSRF 공격 방지
                    .maxAge(Duration.ofHours(1)) // 1시간 유효
                    .build();
            
            response.addHeader("Set-Cookie", cookie.toString());
            
            // 프론트엔드 메인 페이지로 리다이렉트
            response.sendRedirect(frontendUrl + "/");

        } catch (CustomException e) {
            log.error("카카오 OAuth2 처리 중 오류 발생: {}", e.getMessage(), e);
            // 보안: 내부 에러 코드 노출 방지, 일반적인 에러 메시지 사용
            response.sendRedirect(frontendUrl + "/login?error=social_login_failed");
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 예상치 못한 오류 발생", e);
            // 보안: 내부 에러 코드 노출 방지, 일반적인 에러 메시지 사용
            response.sendRedirect(frontendUrl + "/login?error=social_login_failed");
        }
    }
    
    /**
     * 프론트엔드용 카카오 로그인 토큰 교환 (기존 API 유지)
     * 프론트엔드에서 받은 인가 코드로 JWT 토큰을 생성
     * @param code 카카오 인가 코드
     * @return 로그인 응답
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponseDto>> exchangeKakaoToken(@RequestParam String code) {
        try {
            log.info("프론트엔드 카카오 토큰 교환 시작: code={}", code);
            LoginResponseDto response = kakaoService.kakaoLogin(code);
            log.info("카카오 토큰 교환 성공: username={}", response.getUsername());
            return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "카카오 로그인 성공", response)
            );
        } catch (CustomException e) {
            log.error("카카오 토큰 교환 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(
                    e.getErrorCode().getStatus(),
                    e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            log.error("카카오 토큰 교환 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "서버 오류가 발생했습니다.",
                    null
                ));
        }
    }
    
    /**
     * 카카오 로그인 URL 생성
     * @return 카카오 로그인 URL
     */
    @GetMapping("/login")
    public ResponseEntity<ApiResponse<String>> getKakaoLoginUrl() {
        if (kakaoClientId == null || kakaoRedirectUri == null) {
            log.error("카카오 OAuth2 설정이 누락되었습니다");
            throw new CustomException(KakaoErrorCode.KAKAO_CONFIG_MISSING);
        }
        
        String loginUrl = String.format(
            "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
            kakaoClientId, kakaoRedirectUri
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(HttpStatus.OK, "카카오 로그인 URL이 생성되었습니다.", loginUrl)
        );
    }
} 