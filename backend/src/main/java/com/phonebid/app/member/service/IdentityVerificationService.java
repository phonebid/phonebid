package com.phonebid.app.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.IdentityVerificationLog;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.response.IdentityVerificationResponseDto;
import com.phonebid.app.member.repository.IdentityVerificationLogRepository;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.trade.service.PortOneClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityVerificationService {

    private final PortOneClient portOneClient;
    private final UserRepository userRepository;
    private final IdentityVerificationLogRepository logRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public IdentityVerificationResponseDto verifyIdentity(String username, String identityVerificationId) {
        User user = userRepository.findByUsername(username)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsIdentityVerified())) {
            throw new CustomException(CommonErrorCode.ALREADY_VERIFIED);
        }

        // 포트원 API로 본인인증 결과 조회
        String responseBody;
        try {
            responseBody = portOneClient.getIdentityVerification(identityVerificationId)
                    .blockOptional()
                    .orElseThrow(() -> new CustomException(CommonErrorCode.EXTERNAL_SERVICE_ERROR));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("포트원 본인인증 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(CommonErrorCode.EXTERNAL_SERVICE_ERROR);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText();

            if (!"VERIFIED".equals(status)) {
                throw new CustomException(CommonErrorCode.IDENTITY_VERIFICATION_FAILED);
            }

            JsonNode customer = root.path("verifiedCustomer");
            String verifiedName = customer.path("name").asText();
            String verifiedPhone = customer.path("phoneNumber").asText();
            String birthDate = customer.path("birthDate").asText(null);
            String ci = customer.path("ci").asText(null);
            String di = customer.path("di").asText(null);
            String operatorStr = customer.path("operator").asText(null);

            // CI 중복 검사
            if (ci != null) {
                logRepository.findFirstByCi(ci).ifPresent(existing -> {
                    if (!existing.getUser().getId().equals(user.getId())) {
                        throw new CustomException(CommonErrorCode.DUPLICATE_IDENTITY);
                    }
                });
            }

            // 통신사 매핑
            Carrier carrier = mapCarrier(operatorStr);

            // User 엔티티 업데이트
            user.completeIdentityVerification(verifiedName, verifiedPhone, carrier);

            // 감사 로그 저장
            String provider = root.path("requestedCustomer").path("identityProvider").asText(null);

            IdentityVerificationLog verificationLog = IdentityVerificationLog.builder()
                    .user(user)
                    .ci(ci)
                    .di(di)
                    .verifiedName(verifiedName)
                    .verifiedPhone(verifiedPhone)
                    .verifiedBirth(birthDate)
                    .carrier(operatorStr)
                    .provider(provider)
                    .build();
            logRepository.save(verificationLog);

            return IdentityVerificationResponseDto.builder()
                    .verified(true)
                    .name(verifiedName)
                    .phone(verifiedPhone)
                    .carrier(carrier != null ? carrier.getDisplayName() : null)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("본인인증 응답 파싱 실패: {}", e.getMessage(), e);
            throw new CustomException(CommonErrorCode.IDENTITY_VERIFICATION_FAILED);
        }
    }

    private Carrier mapCarrier(String operator) {
        if (operator == null) return null;
        return switch (operator.toUpperCase()) {
            case "SKT", "SK텔레콤" -> Carrier.SKT;
            case "KT" -> Carrier.KT;
            case "LGU+", "LG유플러스", "LGU" -> Carrier.LGU;
            case "SKT_MVNO", "SKT알뜰폰" -> Carrier.SKT_ALD;
            case "KT_MVNO", "KT알뜰폰" -> Carrier.KT_ALD;
            case "LGU+_MVNO", "LGU+알뜰폰" -> Carrier.LGU_ALD;
            default -> {
                log.warn("알 수 없는 통신사: {}", operator);
                yield null;
            }
        };
    }
}
