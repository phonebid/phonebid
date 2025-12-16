package com.phonebid.app.customerservice.controller.admin;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.customerservice.dto.request.NoticeCreateRequestDto;
import com.phonebid.app.customerservice.dto.request.NoticeUpdateRequestDto;
import com.phonebid.app.customerservice.service.NoticeService;
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
@RequestMapping("/api/v1/admin/customerservice/notices")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createNotice(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @Valid @RequestBody NoticeCreateRequestDto requestDto) {

        noticeService.createNotice(userDetails.getUsername(), requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "공지사항이 성공적으로 작성되었습니다.", null));
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @PathVariable UUID noticeId,
                                                          @Valid @RequestBody NoticeUpdateRequestDto requestDto) {

        noticeService.updateNotice(userDetails.getUsername(), noticeId, requestDto);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "공지사항 수정이 성공적으로 완료되었습니다.", null));
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @PathVariable UUID noticeId) {

        noticeService.deleteNotice(userDetails.getUsername(), noticeId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "공지사항 삭제가 성공적으로 완료되었습니다.", null));
    }
}

