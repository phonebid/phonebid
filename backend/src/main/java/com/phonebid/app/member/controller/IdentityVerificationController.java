package com.phonebid.app.member.controller;

import com.phonebid.app.common.config.PortOneV2Properties;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.member.dto.request.IdentityVerificationRequestDto;
import com.phonebid.app.member.dto.response.IdentityVerificationResponseDto;
import com.phonebid.app.member.service.IdentityVerificationService;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/identity-verification")
public class IdentityVerificationController {

    private final IdentityVerificationService identityVerificationService;
    private final PortOneV2Properties portOneV2Properties;

    @GetMapping("/init")
    public ResponseEntity<ApiResponse<Map<String, String>>> getInitInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, String> initInfo = Map.of(
                "storeId", portOneV2Properties.getStoreId(),
                "channelKey", portOneV2Properties.getIdentityChannelKey()
        );
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "본인인증 초기화 정보", initInfo));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<IdentityVerificationResponseDto>> confirmVerification(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody IdentityVerificationRequestDto requestDto) {

        IdentityVerificationResponseDto response = identityVerificationService
                .verifyIdentity(userDetails.getUsername(), requestDto.getIdentityVerificationId());

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "본인인증이 완료되었습니다.", response));
    }
}
