package com.phonebid.app.trade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.trade.dto.request.PgPaymentRequest;
import com.phonebid.app.trade.dto.response.PortOnePaymentInitResponse;
import com.phonebid.app.trade.dto.response.PortOnePaymentStatusResponse;
import com.phonebid.app.trade.service.PortOnePaymentService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments/portone")
@RequiredArgsConstructor
public class PortOnePaymentController {

    private final PortOnePaymentService portOnePaymentService;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<PortOnePaymentInitResponse>> initializePayment(@Valid @RequestBody PgPaymentRequest request) {
        PortOnePaymentInitResponse response = portOnePaymentService.preparePayment(request);
        ApiResponse<PortOnePaymentInitResponse> body = ApiResponse.success(
                HttpStatus.OK,
                "PortOne 결제 초기화 성공",
                response
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PortOnePaymentStatusResponse>> confirmPayment(@RequestBody PortOnePaymentConfirmRequest request) {
        PortOnePaymentStatusResponse status = portOnePaymentService.verifyPayment(request.paymentId());
        ApiResponse<PortOnePaymentStatusResponse> body = ApiResponse.success(
                HttpStatus.OK,
                "PortOne 결제 검증 완료",
                status
        );
        return ResponseEntity.ok(body);
    }
}

