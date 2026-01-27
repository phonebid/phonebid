package com.phonebid.app.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.common.Constants;
import com.phonebid.app.common.util.CookieUtil;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
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
        
        // 쿠키 생성
        boolean isProduction = CookieUtil.isProduction(environment);
        
        // keepLoggedIn 값에 따라 Access Token 쿠키 만료 시간 설정
        Duration accessTokenCookieMaxAge = Boolean.TRUE.equals(requestDto.getKeepLoggedIn())
            ? Constants.Jwt.KEEP_LOGGED_IN_EXPIRY // 30일 유효
            : Constants.Jwt.DEFAULT_EXPIRY; // 1시간 유효
        
        // Access Token 쿠키 생성
        ResponseCookie accessTokenCookie = CookieUtil.createAccessTokenCookie(
            responseDto.getAccessToken(), isProduction, accessTokenCookieMaxAge);

        // Refresh Token 쿠키 생성 (DTO에서 직접 가져오기 - 동시성 문제 해결)
        ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(
            responseDto.getRefreshToken(), isProduction);
        
        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success(HttpStatus.OK, "로그인이 성공적으로 완료되었습니다.", responseDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String username = getCurrentUsername();
        
        // Refresh Token 삭제 (서비스 레이어에서 처리)
        userService.logout(username);
        
        // 쿠키 삭제를 위한 빈 쿠키 설정
        boolean isProduction = CookieUtil.isProduction(environment);
        
        ResponseCookie accessTokenCookie = CookieUtil.createAccessTokenDeleteCookie(isProduction);
        ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenDeleteCookie(isProduction);
        
        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success(HttpStatus.OK, "로그아웃이 성공적으로 완료되었습니다.", null));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile() {
        String username = getCurrentUsername();
        
        // 회원 탈퇴 처리 (서비스 레이어에서 RefreshToken 삭제 포함)
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
            throw new CustomException(CommonErrorCode.AUTHENTICATION_ERROR);
        }
        return authentication.getName();
    }
}
