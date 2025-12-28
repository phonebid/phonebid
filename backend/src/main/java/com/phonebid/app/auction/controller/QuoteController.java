package com.phonebid.app.auction.controller;

import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.dto.response.BidListResponseDto;
import com.phonebid.app.auction.dto.response.QuoteResponseDto;
import com.phonebid.app.auction.service.BidService;
import com.phonebid.app.auction.service.QuoteService;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 견적 컨트롤러
 * 견적 관리 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auction/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final BidService bidService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createQuote(@RequestBody @Valid QuoteCreateRequestDto quoteRequestDto,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        quoteService.createQuote(quoteRequestDto, userDetails.getUser());
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "견적 생성이 성공적으로 완료되었습니다.", null));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<QuoteResponseDto>>> getMyOpenQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuoteResponseDto> quotes = quoteService.getMyOpenQuotes(userDetails.getUser(), pageable);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "내 견적 조회가 성공적으로 완료되었습니다.", quotes));
    }

    /**
     * 진행중인 전체 견적 목록 조회
     * - 관리자(ADMIN) 및 판매자(SELLER)만 접근 가능
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<List<QuoteResponseDto>>> getAllOpenQuotes() {
        
        List<QuoteResponseDto> quotes = quoteService.getAllOpenQuotes();
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "진행중인 견적 조회가 성공적으로 완료되었습니다.", quotes));
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<ApiResponse<QuoteResponseDto>> getQuoteById(@PathVariable UUID quoteId) {
        QuoteResponseDto quote = quoteService.getQuoteById(quoteId);
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "견적 상세 조회가 성공적으로 완료되었습니다.", quote));
    }

    /**
     * 특정 견적의 입찰 목록 조회
     * - 견적 소유자(소비자): 모든 입찰 목록 조회 가능
     * - 판매자: 자신이 입찰한 입찰만 조회 가능
     * - 관리자: 모든 입찰 목록 조회 가능
     */
    @GetMapping("/{quoteId}/bids")
    public ResponseEntity<ApiResponse<List<BidListResponseDto>>> getBidsByQuoteId(@PathVariable UUID quoteId,
                                                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BidListResponseDto> bids = bidService.getBidsByQuoteId(quoteId, userDetails.getUser());
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "견적 별 입찰 목록 조회가 성공적으로 완료되었습니다.", bids));
    }

    @GetMapping("/my/completed")
    public ResponseEntity<ApiResponse<Page<QuoteResponseDto>>> getMyCompletedQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuoteResponseDto> quotes = quoteService.getMyCompletedQuotes(userDetails.getUser(), pageable);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "완료된 견적 조회가 성공적으로 완료되었습니다.", quotes));
    }

    /**
     * 견적 종료
     * - 견적 소유자만 종료 가능
     * - OPEN 상태인 견적만 종료 가능
     */
    @PutMapping("/{quoteId}/close")
    public ResponseEntity<ApiResponse<Void>> closeQuote(@PathVariable UUID quoteId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        quoteService.closeQuote(quoteId, userDetails.getUser());
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "견적이 성공적으로 종료되었습니다.", null));
    }

}
