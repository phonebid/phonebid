package com.phonebid.app.auction.controller;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PricePlanCategory;
import com.phonebid.app.auction.dto.response.PricePlanResponseDto;
import com.phonebid.app.auction.service.PricePlanService;
import com.phonebid.app.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auction/price-plans")
public class PricePlanController {

    private final PricePlanService pricePlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PricePlanResponseDto>>> getActivePricePlans(
            @RequestParam(required = false) Carrier carrier,
            @RequestParam(required = false) PricePlanCategory category) {

        List<PricePlanResponseDto> response = pricePlanService.getActivePricePlans(carrier, category);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "활성 요금제 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PricePlanResponseDto>> getPricePlanById(@PathVariable UUID id) {

        PricePlanResponseDto response = pricePlanService.getPricePlanById(id);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "요금제 상세 조회가 완료되었습니다.", response));
    }
}
