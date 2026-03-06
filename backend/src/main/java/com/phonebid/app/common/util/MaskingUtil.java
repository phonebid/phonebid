package com.phonebid.app.common.util;

/**
 * 개인정보 마스킹 유틸리티 클래스
 * 로그 출력 시 민감한 정보를 안전하게 마스킹하여 개인정보 노출을 방지
 */
public class MaskingUtil {

    private static final String MASKED_VALUE = "***";
    private static final String MASK_PATTERN = "****";

    /**
     * 전화번호 마스킹
     * 앞 3자리와 뒤 4자리를 제외한 중간 부분을 마스킹 처리
     * 
     * @param phoneNumber 원본 전화번호 (예: 01012345678)
     * @return 마스킹된 전화번호 (예: 010****5678)
     * 
     * <pre>
     * 예시:
     * - "01012345678" → "010****5678"
     * - "0212345678" → "021****5678"
     * - null → "***"
     * - "123" → "***"
     * </pre>
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return MASKED_VALUE;
        }
        
        return phoneNumber.substring(0, 3) + MASK_PATTERN + 
               phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * 계좌번호 마스킹
     * 앞 4자리와 뒤 4자리를 제외한 중간 부분을 마스킹 처리
     * 
     * @param accountNumber 원본 계좌번호
     * @return 마스킹된 계좌번호 (예: 1234-****-5678)
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return accountNumber;
        }
        
        int length = accountNumber.length();
        return accountNumber.substring(0, 4) + "-" + MASK_PATTERN + "-" + 
               accountNumber.substring(length - 4);
    }

    /**
     * 이메일 마스킹
     * @ 앞의 아이디 부분을 마스킹 처리 (앞 2자리만 표시)
     * 
     * @param email 원본 이메일
     * @return 마스킹된 이메일 (예: ab****@example.com)
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return MASKED_VALUE;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].length() < 2) {
            return MASKED_VALUE;
        }
        
        String localPart = parts[0];
        String domain = parts[1];
        
        return localPart.substring(0, 2) + MASK_PATTERN + "@" + domain;
    }

    /**
     * 이름 마스킹
     * 성을 제외한 이름 부분을 마스킹 처리
     * 
     * @param name 원본 이름
     * @return 마스킹된 이름 (예: 홍**)
     */
    public static String maskName(String name) {
        if (name == null || name.length() < 2) {
            return MASKED_VALUE;
        }
        
        return name.substring(0, 1) + "*".repeat(name.length() - 1);
    }

    private MaskingUtil() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
}
