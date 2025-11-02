package com.phonebid.app.auction.controller;

import com.phonebid.app.auction.service.QuoteService;
import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.dto.response.QuoteResponseDto;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.security.UserDetailsImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 판매자 문서 컨트롤러
 * 판매자 문서 업로드 및 관리 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auction/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createQuote(@RequestBody QuoteCreateRequestDto quoteRequestDto) {
        // JWT 토큰에서 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        quoteService.createQuote(quoteRequestDto, userDetails.getUser());
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "견적 생성이 성공적으로 완료되었습니다.", null));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<QuoteResponseDto>>> getMyOpenQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // JWT 토큰에서 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Pageable pageable = PageRequest.of(page, size);
        List<QuoteResponseDto> quotes = quoteService.getMyOpenQuotes(userDetails.getUser(), pageable);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "내 견적 조회가 성공적으로 완료되었습니다.", quotes));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuoteResponseDto>>> getAllOpenQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        List<QuoteResponseDto> quotes = quoteService.getAllOpenQuotes(pageable);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "진행중인 견적 조회가 성공적으로 완료되었습니다.", quotes));
    }

}
