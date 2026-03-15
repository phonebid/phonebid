package com.phonebid.app.notification.dto.aligo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 알리고 API 응답 DTO
 */
@Getter
@ToString
@RequiredArgsConstructor
public class AligoResponse {
    
    /**
     * 결과 코드
     * 1: 성공
     * 기타: 실패
     */
    @JsonProperty("result_code")
    private final int resultCode;
    
    /**
     * 응답 메시지
     */
    private final String message;
    
    /**
     * 발송된 메시지 수
     */
    @JsonProperty("msg_count")
    private final Integer msgCount;
    
    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return resultCode == 1;
    }
}
