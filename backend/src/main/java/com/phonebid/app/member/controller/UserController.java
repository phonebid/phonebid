package com.phonebid.app.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.common.Constants;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.dto.request.SignupRequestDto;
import com.phonebid.app.member.dto.request.LoginRequestDto;
import com.phonebid.app.member.dto.request.PasswordChangeRequestDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.service.UserService;
import com.phonebid.app.auth.service.RefreshTokenService;
import com.phonebid.app.member.repository.UserRepository;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final Environment environment;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "회원가입이 성공적으로 완료되었습니다.", null));
    }

    @PostMapping(value = "/login", consumes = {"application/json", "text/plain"})
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = userService.login(requestDto);
        
        // Access Token을 쿠키에 저장
        String accessToken = responseDto.getAccessToken();
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        // Bearer 접두사 제거하여 쿠키에 저장
        String accessTokenValue = accessToken.startsWith(JwtUtil.BEARER_PREFIX) 
            ? accessToken.substring(JwtUtil.BEARER_PREFIX.length()) 
            : accessToken;
        
        // keepLoggedIn 값에 따라 Access Token 쿠키 만료 시간 설정
        Duration accessTokenCookieMaxAge = Boolean.TRUE.equals(requestDto.getKeepLoggedIn())
            ? Constants.Jwt.KEEP_LOGGED_IN_EXPIRY // 30일 유효
            : Constants.Jwt.DEFAULT_EXPIRY; // 1시간 유효
        
        ResponseCookie accessTokenCookie = ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, accessTokenValue)
                .path("/")
                .httpOnly(true) // XSS 공격 방지
                .secure(isProduction) // 프로덕션에서만 HTTPS 필수
                .sameSite("Strict") // CSRF 공격 방지
                .maxAge(accessTokenCookieMaxAge)
                .build();

        // Refresh Token을 쿠키에 저장 (UserService에서 이미 생성됨)
        String refreshToken = refreshTokenService.findByUserId(
            userRepository.findByUsername(responseDto.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getId()
        ).orElseThrow(() -> new RuntimeException("Refresh Token을 찾을 수 없습니다."))
            .getToken();
        
        ResponseCookie refreshTokenCookie = ResponseCookie.from(Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path("/")
                .httpOnly(true) // XSS 공격 방지
                .secure(isProduction) // 프로덕션에서만 HTTPS 필수
                .sameSite("Strict") // CSRF 공격 방지
                .maxAge(Constants.Jwt.REFRESH_TOKEN_EXPIRY) // 30일
                .build();
        
        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success(HttpStatus.OK, "로그인이 성공적으로 완료되었습니다.", responseDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String username = getCurrentUsername();
        UUID userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
            .getId();
        
        // Refresh Token 삭제
        refreshTokenService.deleteByUserId(userId);
        
        // 쿠키 삭제를 위한 빈 쿠키 설정
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        ResponseCookie accessTokenCookie = ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, "")
                .path("/")
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
        
        ResponseCookie refreshTokenCookie = ResponseCookie.from(Constants.Jwt.REFRESH_TOKEN_COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
        
        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success(HttpStatus.OK, "로그아웃이 성공적으로 완료되었습니다.", null));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile() {
        String username = getCurrentUsername();
        UUID userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
            .getId();
        
        // Refresh Token 삭제
        refreshTokenService.deleteByUserId(userId);
        
        userService.deleteProfile(username);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "회원 탈퇴가 성공적으로 완료되었습니다.", null));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequestDto requestDto) {
        String username = getCurrentUsername();
        userService.changePassword(username, requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "비밀번호 변경이 성공적으로 완료되었습니다.", null));
    }

    /**
     * 현재 인증된 사용자의 username을 가져오는 헬퍼 메서드
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return authentication.getName();
    }
}
