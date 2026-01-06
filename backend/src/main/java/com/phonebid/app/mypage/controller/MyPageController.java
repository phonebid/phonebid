package com.phonebid.app.mypage.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.mypage.dto.request.AccountCreateRequestDto;
import com.phonebid.app.mypage.dto.request.DeliveryAddressCreateRequestDto;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.AccountResponseDto;
import com.phonebid.app.mypage.dto.response.DeliveryAddressResponseDto;
import com.phonebid.app.mypage.dto.response.ProfileImageUploadResponseDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseDetailResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseHistoryResponseDto;
import com.phonebid.app.mypage.service.MyPageService;
import org.springframework.web.multipart.MultipartFile;
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

    /**
     * 계좌 등록
     * 사용자의 계좌 정보를 등록합니다. 동일한 은행과 계좌번호 조합은 중복 등록할 수 없습니다.
     */
    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<Void>> createAccount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @Valid @RequestBody AccountCreateRequestDto requestDto) {
        
        myPageService.createAccount(userDetails.getUsername(), requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "계좌 등록이 성공적으로 완료되었습니다.", null));
    }

    /**
     * 계좌 목록 조회
     * 사용자의 등록된 계좌 목록을 페이징하여 조회합니다. 등록일 기준 내림차순으로 정렬하여 반환합니다.
     */
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<Page<AccountResponseDto>>> getAccounts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<AccountResponseDto> responseDto = myPageService.getAccounts(
            userDetails.getUsername(), page, size);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "계좌 목록 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 계좌 삭제
     * 사용자의 계좌를 삭제합니다. 본인의 계좌만 삭제할 수 있습니다.
     */
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable UUID accountId) {
        
        myPageService.deleteAccount(userDetails.getUsername(), accountId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "계좌 삭제가 성공적으로 완료되었습니다.", null));
    }

    /**
     * 기본 배송지 조회
     * 사용자의 기본 배송지를 조회합니다.
     */
    @GetMapping("/delivery-addresses/default")
    public ResponseEntity<ApiResponse<DeliveryAddressResponseDto>> getDefaultDeliveryAddress(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        DeliveryAddressResponseDto responseDto = myPageService.getDefaultDeliveryAddress(userDetails.getUsername());
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "기본 배송지 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 배송지 목록 조회
     * 사용자의 배송지 목록을 페이징하여 조회합니다.
     */
    @GetMapping("/delivery-addresses")
    public ResponseEntity<ApiResponse<Page<DeliveryAddressResponseDto>>> getDeliveryAddresses(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DeliveryAddressResponseDto> responseDto = myPageService.getDeliveryAddresses(
            userDetails.getUsername(), page, size);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "배송지 목록 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 배송지 저장
     * 사용자의 배송지를 저장합니다. 기본 배송지로 저장하는 경우 기존 기본 배송지가 해제됩니다.
     */
    @PostMapping("/delivery-addresses")
    public ResponseEntity<ApiResponse<Void>> createDeliveryAddress(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                   @Valid @RequestBody DeliveryAddressCreateRequestDto requestDto) {
        myPageService.createDeliveryAddress(userDetails.getUsername(), requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "배송지 등록이 성공적으로 완료되었습니다.", null));
    }

    /**
     * 기본 배송지 설정
     * 특정 배송지를 기본 배송지로 설정합니다. 기존 기본 배송지가 해제됩니다.
     */
    @PutMapping("/delivery-addresses/{addressId}/set-default")
    public ResponseEntity<ApiResponse<Void>> setDefaultDeliveryAddress(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                       @PathVariable UUID addressId) {
        myPageService.setDefaultDeliveryAddress(userDetails.getUsername(), addressId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "기본 배송지 설정이 성공적으로 완료되었습니다.", null));
    }

    /**
     * 배송지 삭제
     * 사용자의 배송지를 삭제합니다. 본인의 배송지만 삭제할 수 있습니다.
     */
    @DeleteMapping("/delivery-addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteDeliveryAddress(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                   @PathVariable UUID addressId) {
        myPageService.deleteDeliveryAddress(userDetails.getUsername(), addressId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "배송지 삭제가 성공적으로 완료되었습니다.", null));
    }

    /**
     * 프로필 이미지 업로드
     * 로그인한 사용자의 프로필 사진을 업로드합니다. 기존 프로필 사진이 있으면 자동으로 삭제됩니다.
     */
    @PostMapping("/profile/image")
    public ResponseEntity<ApiResponse<ProfileImageUploadResponseDto>> uploadProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) {
        
        ProfileImageUploadResponseDto responseDto = myPageService.uploadProfileImage(userDetails.getUsername(), file);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "프로필 이미지 업로드가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 프로필 이미지 삭제
     * 로그인한 사용자의 프로필 사진을 삭제합니다.
     */
    @DeleteMapping("/profile/image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        myPageService.deleteProfileImage(userDetails.getUsername());
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "프로필 이미지 삭제가 성공적으로 완료되었습니다.", null));
    }
}

