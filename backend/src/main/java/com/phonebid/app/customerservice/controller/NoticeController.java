package com.phonebid.app.customerservice.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.customerservice.dto.response.NoticeDetailResponseDto;
import com.phonebid.app.customerservice.dto.response.NoticeResponseDto;
import com.phonebid.app.customerservice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/customerservice/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponseDto>>> getAllNotices(@RequestParam(defaultValue = "0") int page, 
                                                                              @RequestParam(defaultValue = "10") int size) {

        Page<NoticeResponseDto> responseDto = noticeService.getAllNotices(page, size);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "공지사항 목록 조회가 성공적으로 완료되었습니다.", responseDto));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> getNoticeDetail(@PathVariable UUID noticeId) {

        NoticeDetailResponseDto responseDto = noticeService.getNoticeDetail(noticeId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "공지사항 상세 조회가 성공적으로 완료되었습니다.", responseDto));
    }
}

