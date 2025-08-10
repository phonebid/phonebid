package com.phonebid.app.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.member.dto.request.SignupRequestDto;
import com.phonebid.app.member.dto.request.LoginRequestDto;
import com.phonebid.app.member.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.member.dto.request.PasswordChangeRequestDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.dto.response.ProfileResponseDto;
import com.phonebid.app.member.service.UserService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "회원가입이 성공적으로 완료되었습니다.", null));
    }

    @PostMapping(value = "/login", consumes = {"application/json", "text/plain"})
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = userService.login(requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "로그인이 성공적으로 완료되었습니다.", responseDto));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getProfile() {
        String username = getCurrentUsername();
        ProfileResponseDto responseDto = userService.getProfile(username);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "내 정보 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        String username = getCurrentUsername();
        userService.updateProfile(username, requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "내 정보 수정이 성공적으로 완료되었습니다.", null));
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
