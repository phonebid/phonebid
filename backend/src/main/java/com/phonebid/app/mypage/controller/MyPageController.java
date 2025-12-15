package com.phonebid.app.mypage.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import com.phonebid.app.mypage.service.MyPageService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 프로필 조회
     * 로그인한 사용자의 프로필 정보(아이디, 닉네임, 휴대폰 번호, 이름)를 조회합니다.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ProfileResponseDto responseDto = myPageService.getProfile(userDetails.getUsername());
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "프로필 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 프로필 수정
     * 로그인한 사용자의 프로필 정보(이름, 닉네임, 휴대폰 번호)를 수정합니다.
     * 각 필드는 선택적으로 수정 가능하며, 닉네임 중복 검증을 수행합니다.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        
        myPageService.updateProfile(userDetails.getUsername(), requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "프로필 수정이 성공적으로 완료되었습니다.", null));
    }
}

