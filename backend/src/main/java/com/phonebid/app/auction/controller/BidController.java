package com.phonebid.app.auction.controller;

import com.phonebid.app.auction.dto.request.BidCreateRequestDto;
import com.phonebid.app.auction.dto.request.BidUpdateRequestDto;
import com.phonebid.app.auction.dto.response.BidListResponseDto;
import com.phonebid.app.auction.dto.response.BidResponseDto;
import com.phonebid.app.auction.service.BidService;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auction/bids")
public class BidController {

    private final BidService bidService;

    /**
     * 입찰 생성
     * - 판매자만 입찰 가능
     * - 동일 견적에 여러 번 입찰 가능
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDto>> createBid(@RequestBody @Valid BidCreateRequestDto requestDto, 
                                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        BidResponseDto response = bidService.createBid(requestDto, userDetails.getUser());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "입찰이 성공적으로 생성되었습니다.", response));
    }

    /**
     * 입찰 수정
     * - 본인의 입찰만 수정 가능
     * - ACTIVE 상태이고 견적이 아직 입찰을 받을 수 있는 상태여야 함
     */
    @PutMapping("/{bidId}")
    public ResponseEntity<ApiResponse<BidResponseDto>> updateBid(@PathVariable UUID bidId,
                                                                 @RequestBody @Valid BidUpdateRequestDto requestDto,
                                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        BidResponseDto response = bidService.updateBid(bidId, requestDto, userDetails.getUser());
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "입찰이 성공적으로 수정되었습니다.", response));
    }

    /**
     * 입찰 상세 조회
     */
    @GetMapping("/{bidId}")
    public ResponseEntity<ApiResponse<BidResponseDto>> getBidById(@PathVariable UUID bidId) {
        BidResponseDto response = bidService.getBidById(bidId);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "입찰 상세 조회가 성공적으로 완료되었습니다.", response));
    }

    /**
     * 내 입찰 목록 조회 (판매자 전용)
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BidListResponseDto>>> getMyBids(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Page<BidListResponseDto> response = bidService.getMyBids(userDetails.getUser(), page, size);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "내 입찰 목록 조회가 성공적으로 완료되었습니다.", response));
    }

    /**
     * 특정 견적에 이미 입찰했는지 확인
     */
    @GetMapping("/check/{quoteId}")
    public ResponseEntity<ApiResponse<Boolean>> checkAlreadyBid(@PathVariable UUID quoteId,
                                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        boolean hasBid = bidService.hasAlreadyBid(quoteId, userDetails.getUser());
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "입찰 여부 확인이 완료되었습니다.", hasBid));
    }
}

