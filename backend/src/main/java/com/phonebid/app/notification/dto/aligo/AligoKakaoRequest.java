package com.phonebid.app.notification.dto.aligo;

import lombok.Builder;
import lombok.Getter;

/**
 * 알리고 카카오 알림톡 발송 요청 DTO
 */
@Getter
@Builder
public class AligoKakaoRequest {
    
    /**
     * 수신자 전화번호 (01012345678 형식, 하이픈 없이)
     */
    private String receiver;
    
    /**
     * 템플릿 코드 (알리고에서 승인받은 템플릿 코드)
     */
    private String templateCode;
    
    /**
     * 메시지 내용 (템플릿 변수 포함)
     */
    private String message;
    
    /**
     * 버튼 URL (옵션)
     */
    private String buttonUrl;
    
    /**
     * 버튼 이름 (옵션, 기본값: "자세히보기")
     */
    private String buttonName;
}
