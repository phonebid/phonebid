package com.phonebid.app.auction.controller.admin;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.dto.request.PricePlanCreateRequestDto;
import com.phonebid.app.auction.dto.response.PricePlanResponseDto;
import com.phonebid.app.auction.service.PricePlanService;
import com.phonebid.app.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auction/price-plans")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricePlanController {

    private final PricePlanService pricePlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PricePlanResponseDto>>> getAllPricePlans(
            @RequestParam(required = false) Carrier carrier) {

        List<PricePlanResponseDto> response = pricePlanService.getAllPricePlansForAdmin(carrier);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "요금제 전체 목록 조회가 완료되었습니다.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PricePlanResponseDto>> createPricePlan(
            @Valid @RequestBody PricePlanCreateRequestDto requestDto) {

        PricePlanResponseDto response = pricePlanService.createPricePlan(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "요금제가 성공적으로 등록되었습니다.", response));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivatePricePlan(@PathVariable UUID id) {

        pricePlanService.deactivatePricePlan(id);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "요금제가 비활성화되었습니다.", null));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activatePricePlan(@PathVariable UUID id) {

        pricePlanService.activatePricePlan(id);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "요금제가 활성화되었습니다.", null));
    }

    @PutMapping("/{id}/display-order")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrder(
            @PathVariable UUID id,
            @RequestParam Integer displayOrder) {

        pricePlanService.updateDisplayOrder(id, displayOrder);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "요금제 노출 순서가 변경되었습니다.", null));
    }
}
