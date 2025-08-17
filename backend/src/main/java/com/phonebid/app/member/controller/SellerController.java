package com.phonebid.app.member.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.member.dto.request.SellerRegisterRequestDto;
import com.phonebid.app.member.dto.request.SellerProfileUpdateRequestDto;
import com.phonebid.app.member.dto.response.SellerProfileResponseDto;
import com.phonebid.app.member.service.SellerService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 판매자 컨트롤러
 * 판매자 관련 API 엔드포인트를 제공하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final SellerService sellerService;

    /**
     * 판매자 신청(등록)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerSeller(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SellerRegisterRequestDto requestDto) {
        
        sellerService.registerSeller(userDetails.getUsername(), requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "판매자 신청이 성공적으로 등록되었습니다.", null));
    }

    /**
     * 판매자 프로필 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileResponseDto>> getSellerProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        SellerProfileResponseDto responseDto = sellerService.getSellerProfile(userDetails.getUsername());
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "판매자 프로필 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 판매자 프로필 수정
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateSellerProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SellerProfileUpdateRequestDto requestDto) {
        
        sellerService.updateSellerProfile(userDetails.getUsername(), requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "판매자 프로필 수정이 성공적으로 완료되었습니다.", null));
    }
} 