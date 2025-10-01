package com.phonebid.app.phone.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.phone.service.PhoneOptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.phonebid.app.common.dto.ApiResponse;
import java.util.List;

import com.phonebid.app.phone.dto.request.PhoneOptionCreateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneOptionDeleteRequestDto;
import com.phonebid.app.phone.dto.response.PhoneOptionResponseDto;

/**
 * 휴대폰 모델 컨트롤러
 * 휴대폰 모델 관리 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/phone-options") 
public class PhoneOptionController {

    private final PhoneOptionService phoneOptionService;



    /**
     * 휴대폰 옵션 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<PhoneOptionResponseDto>>> createPhoneOption(@RequestBody List<PhoneOptionCreateRequestDto> requestDto) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 옵션 생성이 성공적으로 완료되었습니다.", phoneOptionService.createPhoneOptions(requestDto)));
    }

    // 휴대폰 옵션 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deletePhoneOption(@RequestBody PhoneOptionDeleteRequestDto requestDto) {
        phoneOptionService.deletePhoneOption(requestDto);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "휴대폰 옵션 삭제이 성공적으로 완료되었습니다.", null));
    }

}
