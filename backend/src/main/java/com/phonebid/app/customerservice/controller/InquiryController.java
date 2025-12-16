package com.phonebid.app.customerservice.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.customerservice.dto.request.InquiryCreateRequestDto;
import com.phonebid.app.customerservice.dto.request.InquiryUpdateRequestDto;
import com.phonebid.app.customerservice.dto.response.InquiryDetailResponseDto;
import com.phonebid.app.customerservice.dto.response.InquiryResponseDto;
import com.phonebid.app.customerservice.service.InquiryService;
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
@RequestMapping("/api/v1/mypage/customerservice/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @Valid @RequestBody InquiryCreateRequestDto requestDto) {

        inquiryService.createInquiry(userDetails.getUsername(), requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "문의가 성공적으로 등록되었습니다.", null));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<InquiryResponseDto>>> getMyInquiries(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<InquiryResponseDto> responseDto = inquiryService.getMyInquiries(
                userDetails.getUsername(), page, size);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "나의 문의내역 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> getInquiryDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                  @PathVariable UUID inquiryId) {

        InquiryDetailResponseDto responseDto = inquiryService.getInquiryDetail(
                userDetails.getUsername(), inquiryId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "문의 상세 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    @PutMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> updateInquiry(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID inquiryId,
            @Valid @RequestBody InquiryUpdateRequestDto requestDto) {

        inquiryService.updateInquiry(userDetails.getUsername(), inquiryId, requestDto);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "문의 수정이 성공적으로 완료되었습니다.", null));
    }

    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable UUID inquiryId) {

        inquiryService.deleteInquiry(userDetails.getUsername(), inquiryId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "문의 삭제가 성공적으로 완료되었습니다.", null));
    }
}

