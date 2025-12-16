package com.phonebid.app.customerservice.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.customerservice.domain.FaqCategory;
import com.phonebid.app.customerservice.dto.response.FaqDetailResponseDto;
import com.phonebid.app.customerservice.dto.response.FaqResponseDto;
import com.phonebid.app.customerservice.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/customerservice/faqs")
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FaqResponseDto>>> getAllFaqs(
            @RequestParam(required = false) FaqCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<FaqResponseDto> responseDto = faqService.getAllFaqs(category, page, size);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "FAQ 목록 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    @GetMapping("/{faqId}")
    public ResponseEntity<ApiResponse<FaqDetailResponseDto>> getFaqDetail(@PathVariable UUID faqId) {

        FaqDetailResponseDto responseDto = faqService.getFaqDetail(faqId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "FAQ 상세 조회가 성공적으로 완료되었습니다.", responseDto));
    }
}

