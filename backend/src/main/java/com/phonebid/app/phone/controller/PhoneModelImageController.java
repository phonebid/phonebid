package com.phonebid.app.phone.controller;

import com.phonebid.app.phone.dto.request.PhoneModelImageUploadRequestDto;
import com.phonebid.app.phone.dto.response.PhoneModelImageResponseDto;
import com.phonebid.app.phone.dto.response.PhoneModelImageUploadResponseDto;
import com.phonebid.app.phone.service.PhoneModelImageService;
import com.phonebid.app.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/phone/models/{phoneModelId}/images")
public class PhoneModelImageController {

    private final PhoneModelImageService phoneModelImageService;

    /**
     * 핸드폰 모델 이미지 업로드
     * 여러 이미지를 업로드할 수 있으며, 최대 10개까지 허용됩니다.
     * 관리자만 사용 가능합니다.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PhoneModelImageUploadResponseDto>> uploadPhoneModelImages(@PathVariable UUID phoneModelId,
                                                                                                @Valid @ModelAttribute PhoneModelImageUploadRequestDto requestDto) {
        
        PhoneModelImageUploadResponseDto responseDto = phoneModelImageService.uploadPhoneModelImages(phoneModelId, requestDto);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "핸드폰 모델 이미지 업로드가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 핸드폰 모델 이미지 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PhoneModelImageResponseDto>>> getPhoneModelImages(@PathVariable UUID phoneModelId) {
        
        List<PhoneModelImageResponseDto> images = phoneModelImageService.getPhoneModelImages(phoneModelId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "핸드폰 모델 이미지 목록 조회가 성공적으로 완료되었습니다.", images));
    }

    /**
     * 핸드폰 모델 이미지 삭제
     * 관리자만 사용 가능합니다.
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePhoneModelImage(@PathVariable UUID phoneModelId,
                                                                   @PathVariable UUID imageId) {
        
        phoneModelImageService.deletePhoneModelImage(phoneModelId, imageId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "핸드폰 모델 이미지 삭제가 성공적으로 완료되었습니다.", null));
    }
}

