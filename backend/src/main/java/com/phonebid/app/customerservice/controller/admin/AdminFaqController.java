package com.phonebid.app.customerservice.controller.admin;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.customerservice.dto.request.FaqCreateRequestDto;
import com.phonebid.app.customerservice.dto.request.FaqUpdateRequestDto;
import com.phonebid.app.customerservice.service.FaqService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/customerservice/faqs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFaqController {

    private final FaqService faqService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createFaq(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @Valid @RequestBody FaqCreateRequestDto requestDto) {

        faqService.createFaq(userDetails.getUsername(), requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "FAQ가 성공적으로 작성되었습니다.", null));
    }

    @PutMapping("/{faqId}")
    public ResponseEntity<ApiResponse<Void>> updateFaq(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID faqId,
            @Valid @RequestBody FaqUpdateRequestDto requestDto) {

        faqService.updateFaq(userDetails.getUsername(), faqId, requestDto);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "FAQ 수정이 성공적으로 완료되었습니다.", null));
    }

    @DeleteMapping("/{faqId}")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @PathVariable UUID faqId) {

        faqService.deleteFaq(userDetails.getUsername(), faqId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "FAQ 삭제가 성공적으로 완료되었습니다.", null));
    }
}

