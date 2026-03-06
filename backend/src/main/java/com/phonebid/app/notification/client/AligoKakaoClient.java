package com.phonebid.app.notification.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.common.errorcode.NotificationErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.util.MaskingUtil;
import com.phonebid.app.notification.config.AligoProperties;
import com.phonebid.app.notification.dto.aligo.AligoKakaoRequest;
import com.phonebid.app.notification.dto.aligo.AligoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 알리고 카카오 알림톡 API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AligoKakaoClient {
    
    @Qualifier("aligoWebClient")
    private final WebClient webClient;
    
    private final AligoProperties aligoProperties;
    private final ObjectMapper objectMapper;
    
    /**
     * 카카오 알림톡 발송
     * 
     * @param request 알림톡 발송 요청
     * @return 발송 결과
     */
    public Mono<AligoResponse> sendKakaoNotification(AligoKakaoRequest request) {
        MultiValueMap<String, String> formData = buildFormData(request);
        String maskedReceiver = MaskingUtil.maskPhoneNumber(request.getReceiver());
        
        return webClient.post()
                .uri("/akv10/alimtalk/send/")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("알리고 API HTTP 오류: status={}, body={}", 
                                     response.statusCode(), errorBody);
                            return Mono.error(new CustomException(
                                NotificationErrorCode.KAKAO_ALIMTALK_API_ERROR));
                        })
                )
                .bodyToMono(AligoResponse.class)
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        log.debug("알림톡 발송 성공: receiver={}, resultCode={}, msgCount={}", 
                                 maskedReceiver, response.getResultCode(), response.getMsgCount());
                    } else {
                        log.warn("알림톡 발송 실패: receiver={}, resultCode={}, message={}", 
                                maskedReceiver, response.getResultCode(), response.getMessage());
                    }
                })
                .doOnError(error -> 
                    log.error("알림톡 발송 중 예외 발생: receiver={}, error={}", 
                             maskedReceiver, error.getMessage(), error)
                );
    }
    
    /**
     * Form 데이터 구성
     */
    private MultiValueMap<String, String> buildFormData(AligoKakaoRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        
        // 필수 파라미터
        formData.add("userid", aligoProperties.getApi().getUserId());
        formData.add("key", aligoProperties.getApi().getApiKey());
        formData.add("sender", aligoProperties.getSender().getKey());
        formData.add("receiver", request.getReceiver());
        formData.add("msg", request.getMessage());
        formData.add("tpl_code", request.getTemplateCode());
        
        // 버튼 정보 (옵션)
        if (request.getButtonUrl() != null && !request.getButtonUrl().isEmpty()) {
            String buttonName = request.getButtonName() != null ? 
                               request.getButtonName() : "자세히보기";
            
            try {
                Map<String, Object> buttonStructure = Map.of(
                    "button", List.of(
                        Map.of(
                            "name", buttonName,
                            "linkType", "WL",
                            "linkTypeName", "웹링크",
                            "linkMo", request.getButtonUrl(),
                            "linkPc", request.getButtonUrl()
                        )
                    )
                );
                
                String buttonJson = objectMapper.writeValueAsString(buttonStructure);
                formData.add("button", buttonJson);
            } catch (JsonProcessingException e) {
                log.error("버튼 JSON 직렬화 실패: buttonName={}, buttonUrl={}", 
                         buttonName, request.getButtonUrl(), e);
                throw new CustomException(NotificationErrorCode.KAKAO_ALIMTALK_API_ERROR);
            }
        }
        
        return formData;
    }
}
