package com.phonebid.app.customerservice.controller.admin;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.customerservice.domain.InquiryCategory;
import com.phonebid.app.customerservice.domain.InquiryStatus;
import com.phonebid.app.customerservice.dto.request.InquiryReplyRequestDto;
import com.phonebid.app.customerservice.dto.response.InquiryResponseDto;
import com.phonebid.app.customerservice.service.InquiryService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/customerservice/inquiries")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InquiryResponseDto>>> getAllInquiries(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<InquiryResponseDto> responseDto = inquiryService.getAllInquiries(userDetails.getUsername(), status, category, page, size);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "전체 문의 목록 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    @PostMapping("/{inquiryId}/reply")
    public ResponseEntity<ApiResponse<Void>> createReply(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID inquiryId,
            @Valid @RequestBody InquiryReplyRequestDto requestDto) {

        inquiryService.createReply(userDetails.getUsername(), inquiryId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "답변이 성공적으로 작성되었습니다.", null));
    }

    @PutMapping("/{inquiryId}/reply/{replyId}")
    public ResponseEntity<ApiResponse<Void>> updateReply(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID inquiryId,
            @PathVariable UUID replyId,
            @Valid @RequestBody InquiryReplyRequestDto requestDto) {

        inquiryService.updateReply(userDetails.getUsername(), inquiryId, replyId, requestDto);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "답변 수정이 성공적으로 완료되었습니다.", null));
    }

    @DeleteMapping("/{inquiryId}/reply/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID inquiryId,
            @PathVariable UUID replyId) {

        inquiryService.deleteReply(userDetails.getUsername(), inquiryId, replyId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "답변 삭제가 성공적으로 완료되었습니다.", null));
    }
}

