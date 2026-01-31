package com.phonebid.app.member.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.NaverErrorCode;
import com.phonebid.app.common.util.CookieUtil;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.service.NaverService;
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

/**
 * 네이버 OAuth2 인증 컨트롤러
 * 네이버 소셜 로그인 관련 엔드포인트를 처리하는 클래스
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/naver")
@RequiredArgsConstructor
public class NaverController {

    private final NaverService naverService;
    private final Environment environment;

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * 네이버 로그인 콜백 처리 (네이버에서 직접 호출)
     * 네이버에서 인가 코드를 받아 JWT 토큰을 생성하고 쿠키에 저장
     * @param code 네이버 인가 코드
     * @param response HTTP 응답 객체
     */
    @GetMapping("/callback")
    public void naverCallback(@RequestParam String code, HttpServletResponse response) throws Exception {
        try {
            log.debug("네이버 로그인 콜백 처리 시작");

            // 인가 코드로 로그인 처리 및 JWT 토큰 생성
            LoginResponseDto loginResponse = naverService.naverLogin(code);
            String token = loginResponse.getAccessToken();

            log.debug("네이버 로그인 성공: username={}", loginResponse.getUsername());

            // Access Token과 Refresh Token을 쿠키에 저장
            boolean isProduction = CookieUtil.isProduction(environment);

            // Access Token 쿠키 생성
            ResponseCookie accessTokenCookie = CookieUtil.createAccessTokenCookie(
                token, isProduction, Duration.ofHours(1));
            
            // Refresh Token 쿠키 생성 (DTO에서 직접 가져오기 - 동시성 문제 해결)
            ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(
                loginResponse.getRefreshToken(), isProduction);

            response.addHeader("Set-Cookie", accessTokenCookie.toString());
            response.addHeader("Set-Cookie", refreshTokenCookie.toString());

            // 프론트엔드 메인 페이지로 리다이렉트
            response.sendRedirect(frontendUrl + "/");

        } catch (CustomException e) {
            log.error("네이버 OAuth2 처리 중 오류 발생: {}", e.getMessage(), e);
            // 보안: 내부 에러 코드 노출 방지, 일반적인 에러 메시지 사용
            response.sendRedirect(frontendUrl + "/login?error=social_login_failed");
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 예상치 못한 오류 발생", e);
            // 보안: 내부 에러 코드 노출 방지, 일반적인 에러 메시지 사용
            response.sendRedirect(frontendUrl + "/login?error=social_login_failed");
        }
    }

    /**
     * 프론트엔드용 네이버 로그인 토큰 교환 (기존 API 유지)
     * 프론트엔드에서 받은 인가 코드로 JWT 토큰을 생성
     * @param code 네이버 인가 코드
     * @return 로그인 응답
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponseDto>> exchangeNaverToken(@RequestParam String code) {
        try {
            log.debug("프론트엔드 네이버 토큰 교환 시작");
            LoginResponseDto response = naverService.naverLogin(code);
            log.debug("네이버 토큰 교환 성공: username={}", response.getUsername());
            return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "네이버 로그인 성공", response)
            );
        } catch (CustomException e) {
            log.error("네이버 토큰 교환 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(
                    e.getErrorCode().getStatus(),
                    e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            log.error("네이버 토큰 교환 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "서버 오류가 발생했습니다.",
                    null
                ));
        }
    }

    /**
     * 네이버 로그인 URL 생성
     * @return 네이버 로그인 URL
     */
    @GetMapping("/login")
    public ResponseEntity<ApiResponse<String>> getNaverLoginUrl() {
        if (naverClientId == null || naverRedirectUri == null) {
            log.error("네이버 OAuth2 설정이 누락되었습니다");
            throw new CustomException(NaverErrorCode.NAVER_CONFIG_MISSING);
        }
        
        String loginUrl = String.format(
            "https://nid.naver.com/oauth2.0/authorize?client_id=%s&redirect_uri=%s&response_type=code&state=naver",
            naverClientId, naverRedirectUri
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(HttpStatus.OK, "네이버 로그인 URL이 생성되었습니다.", loginUrl)
        );
    }
} 