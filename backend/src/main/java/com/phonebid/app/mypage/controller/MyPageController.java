package com.phonebid.app.mypage.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseDetailResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseHistoryResponseDto;
import com.phonebid.app.mypage.service.MyPageService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
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
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        
        myPageService.updateProfile(userDetails.getUsername(), requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "프로필 수정이 성공적으로 완료되었습니다.", null));
    }

    /**
     * 구매내역 목록 조회
     * 구매완료 또는 취소/환불 상태의 구매내역을 페이징하여 조회합니다.
     */
    @GetMapping("/purchases")
    public ResponseEntity<ApiResponse<Page<PurchaseHistoryResponseDto>>> getPurchaseHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "COMPLETED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PurchaseHistoryResponseDto> responseDto = myPageService.getPurchaseHistory(userDetails.getUsername(), status, page, size);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "구매내역 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 구매내역 상세 조회
     * 특정 계약의 상세 정보를 조회합니다.
     */
    @GetMapping("/purchases/{contractId}")
    public ResponseEntity<ApiResponse<PurchaseDetailResponseDto>> getPurchaseDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                    @PathVariable UUID contractId) {
        PurchaseDetailResponseDto responseDto = myPageService.getPurchaseDetail(userDetails.getUsername(), contractId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "구매내역 상세 조회가 성공적으로 완료되었습니다.", responseDto));
    }
}

