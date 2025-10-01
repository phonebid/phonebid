package com.phonebid.app.phone.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.phone.service.PhoneModelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.phonebid.app.common.dto.ApiResponse;
import java.util.List;
import java.util.UUID;

import com.phonebid.app.phone.dto.request.PhoneModelCreateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneModelDeleteRequestDto;
import com.phonebid.app.phone.dto.request.PhoneModelUpdateRequestDto;
import com.phonebid.app.phone.dto.response.PhoneModelResponseDto;

/**
 * 휴대폰 모델 컨트롤러
 * 휴대폰 모델 관리 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/phone/models") 
public class PhoneModelController {

    private final PhoneModelService phoneModelService;

    /**
     * 휴대폰 모델 목록 조회
     * GET /api/v1/phone/models
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PhoneModelResponseDto>>> getPhoneModels() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 모델 목록 조회가 성공적으로 완료되었습니다.", phoneModelService.getPhoneModels()));
    }

    /**
     * 휴대폰 모델 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PhoneModelResponseDto>> createPhoneModel(@RequestBody PhoneModelCreateRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 모델 생성이 성공적으로 완료되었습니다.", phoneModelService.createPhoneModel(requestDto)));
    }

    // 휴대폰 모델 수정
    @PutMapping
    public ResponseEntity<ApiResponse<PhoneModelResponseDto>> updatePhoneModel(@RequestBody PhoneModelUpdateRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 모델 수정이 성공적으로 완료되었습니다.", phoneModelService.updatePhoneModel(requestDto)));
    }

    // 휴대폰 모델 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deletePhoneModel(@RequestBody PhoneModelDeleteRequestDto requestDto) {
        phoneModelService.deletePhoneModel(requestDto);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 모델 삭제이 성공적으로 완료되었습니다.", null));
    }

    // 휴대폰 모델 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PhoneModelResponseDto>> getPhoneModel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 모델 상세 조회가 성공적으로 완료되었습니다.", phoneModelService.getPhoneModel(id)));
    }

}
