package com.phonebid.app.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * RefreshToken 해싱을 위한 유틸리티 클래스
 * HMAC-SHA256을 사용하여 토큰을 해시화합니다.
 */
@Slf4j
@Component
public class TokenHashUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;
    
    private static final String ALGORITHM = "HmacSHA256";

    /**
     * 토큰을 HMAC-SHA256으로 해시화합니다.
     * 
     * @param token 원본 토큰
     * @return 해시화된 토큰 (Base64 인코딩)
     */
    public String hashToken(String token) {
        try {
            Mac macInstance = Mac.getInstance(ALGORITHM);
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            macInstance.init(secretKeySpec);
            
            byte[] hashBytes = macInstance.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("토큰 해시화 실패", e);
            throw new RuntimeException("토큰 해시화 실패", e);
        }
    }
}

