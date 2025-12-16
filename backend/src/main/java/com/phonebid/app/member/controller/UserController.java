package com.phonebid.app.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.dto.request.SignupRequestDto;
import com.phonebid.app.member.dto.request.LoginRequestDto;
import com.phonebid.app.member.dto.request.PasswordChangeRequestDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.service.UserService;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final Environment environment;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "회원가입이 성공적으로 완료되었습니다.", null));
    }

    @PostMapping(value = "/login", consumes = {"application/json", "text/plain"})
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = userService.login(requestDto);
        
        // JWT 토큰을 쿠키에 저장
        String token = responseDto.getAccessToken();
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        // Bearer 접두사 제거하여 쿠키에 저장
        String tokenValue = token.startsWith(JwtUtil.BEARER_PREFIX) 
            ? token.substring(JwtUtil.BEARER_PREFIX.length()) 
            : token;
        
        ResponseCookie cookie = ResponseCookie.from(JwtUtil.AUTHORIZATION_HEADER, tokenValue)
                .path("/")
                .httpOnly(true) // XSS 공격 방지
                .secure(isProduction) // 프로덕션에서만 HTTPS 필수
                .sameSite("Lax") // CSRF 공격 방지
                .maxAge(Duration.ofHours(1)) // 1시간 유효
                .build();
        
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(ApiResponse.success(HttpStatus.OK, "로그인이 성공적으로 완료되었습니다.", responseDto));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile() {
        String username = getCurrentUsername();
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
