package com.phonebid.app.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.KakaoErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.Provider;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.response.KakaoUserInfoDto;
import com.phonebid.app.member.dto.response.LoginResponseDto;
import com.phonebid.app.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * 카카오 OAuth2 인증 서비스
 * 카카오 로그인 및 사용자 정보 처리를 담당하는 클래스
 */
@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    
    @Value("${oauth.kakao.client-id}")
    private String clientId;
    
    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;
    
    /**
     * 카카오 로그인 처리
     * @param code 카카오 인가 코드
     * @return 로그인 응답 DTO
     * @throws CustomException OAuth2 처리 중 오류 발생 시
     */
    @Transactional
    public LoginResponseDto kakaoLogin(String code) throws CustomException {
        try {
            // 1. 인가 코드로 액세스 토큰 요청
            String accessToken = getToken(code);
            
            // 2. 액세스 토큰으로 카카오 사용자 정보 가져오기
            KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
            
            // 3. 필요시 회원가입 처리
            User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);
            
            // 4. JWT 토큰 생성 및 반환
            String token = jwtUtil.createToken(kakaoUser.getUsername(), kakaoUser.getRole());
            
            return LoginResponseDto.of(token, kakaoUser.getUsername(), kakaoUser.getNickname(), kakaoUser.getRole().name());
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 예상치 못한 오류 발생", e);
            throw new CustomException(KakaoErrorCode.KAKAO_LOGIN_PROCESSING_FAILED);
        }
    }
    
    /**
     * 카카오 사용자 정보로 사용자 등록 또는 연동
     * @param kakaoUserInfo 카카오 사용자 정보
     * @return 등록된 사용자 엔티티
     */
    @Transactional
    private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        // DB에 중복된 Kakao providerId가 있는지 확인
        String providerId = String.valueOf(kakaoUserInfo.getId());
        User kakaoUser = userRepository.findByProviderId(providerId).orElse(null);
        
        if (kakaoUser == null) {
            // 카카오 사용자 email과 동일한 email 가진 회원이 있는지 확인
            String kakaoEmail = kakaoUserInfo.getEmail();
            User sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
            
            if (sameEmailUser != null) {
                // 동일한 이메일 사용자가 있으면 카카오 연동
                kakaoUser = sameEmailUser;
                kakaoUser.updateProvider(Provider.KAKAO);
                kakaoUser.updateProviderId(providerId);
                log.info("기존 사용자에 카카오 연동: username={}", kakaoUser.getUsername());
            } else {
                // 신규 회원가입 - 카카오 이메일을 그대로 username으로 사용
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);
                
                // username은 카카오 이메일 그대로 사용
                String username = kakaoUserInfo.getEmail();
                
                kakaoUser = User.builder()
                        .username(username)
                        .password(encodedPassword)
                        .email(kakaoUserInfo.getEmail())
                        .name(kakaoUserInfo.getName())
                        .nickname(kakaoUserInfo.getNickname())
                        .phone(kakaoUserInfo.getPhone())
                        .role(Role.CONSUMER) // 기본값은 소비자
                        .provider(Provider.KAKAO)
                        .providerId(providerId)
                        .build();
                
                log.info("카카오 신규 사용자 등록 완료: username={}", username);
            }
            
            userRepository.save(kakaoUser);
        }
        
        return kakaoUser;
    }
    
    /**
     * 인가 코드로 액세스 토큰 요청
     * @param code 카카오 인가 코드
     * @return 액세스 토큰
     * @throws CustomException 토큰 요청 실패 시
     */
    private String getToken(String code) throws CustomException {
        String tokenUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUriString();
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        
        String response = webClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        if (response == null) {
            throw new CustomException(KakaoErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }
        
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response);
            
            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                String errorDescription = jsonNode.has("error_description") ? 
                    jsonNode.get("error_description").asText() : "알 수 없는 오류";
                log.error("카카오 토큰 요청 실패: {} - {}", error, errorDescription);
                throw new CustomException(KakaoErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
            }
            
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            log.error("카카오 토큰 응답 파싱 실패", e);
            throw new CustomException(KakaoErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }
    
    /**
     * 액세스 토큰으로 카카오 사용자 정보 가져오기
     * @param accessToken 액세스 토큰
     * @return 카카오 사용자 정보 DTO
     * @throws CustomException 사용자 정보 요청 실패 시
     */
    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws CustomException {
        String userInfoUrl = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUriString();
        
        String response = webClient.post()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        if (response == null) {
            throw new CustomException(KakaoErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
        }
        
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response);
            
            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                String errorDescription = jsonNode.has("error_description") ? 
                    jsonNode.get("error_description").asText() : "알 수 없는 오류";
                log.error("카카오 사용자 정보 요청 실패: {} - {}", error, errorDescription);
                throw new CustomException(KakaoErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
            }
            
            // 필수 필드 검증
            if (!jsonNode.has("id")) {
                log.error("카카오 사용자 정보 응답에 id 필드가 없습니다");
                throw new CustomException(KakaoErrorCode.KAKAO_API_RESPONSE_PARSE_FAILED);
            }
            
            JsonNode properties = jsonNode.get("properties");
            if (properties == null || !properties.has("nickname")) {
                log.error("카카오 사용자 정보 응답에 properties.nickname 필드가 없습니다");
                throw new CustomException(KakaoErrorCode.KAKAO_API_RESPONSE_PARSE_FAILED);
            }
            
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            if (kakaoAccount == null || !kakaoAccount.has("email")) {
                log.error("카카오 사용자 정보 응답에 kakao_account.email 필드가 없습니다");
                throw new CustomException(KakaoErrorCode.KAKAO_API_RESPONSE_PARSE_FAILED);
            }
            
            if (!kakaoAccount.has("phone_number")) {
                log.error("카카오 사용자 정보 응답에 kakao_account.phone_number 필드가 없습니다");
                throw new CustomException(KakaoErrorCode.KAKAO_API_RESPONSE_PARSE_FAILED);
            }
            
            // 안전한 필드 추출
            Long id = jsonNode.get("id").asLong();
            String nickname = properties.get("nickname").asText();
            String email = kakaoAccount.get("email").asText();
            String phone = formatPhoneNumber(kakaoAccount.get("phone_number").asText());
            
            // 선택적 필드 처리 (name은 선택사항)
            String name = kakaoAccount.has("name") && !kakaoAccount.get("name").isNull() 
                ? kakaoAccount.get("name").asText() 
                : nickname;
            
            log.info("카카오 사용자 정보 조회 성공: id={}", id);
            return KakaoUserInfoDto.of(id, nickname, email, name, phone);
        } catch (JsonProcessingException e) {
            log.error("카카오 사용자 정보 응답 파싱 실패", e);
            throw new CustomException(KakaoErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
        }
    }
    
    /**
     * 카카오 API에서 받은 국제전화번호를 한국 휴대폰 번호 형식으로 변환합니다.
     * 예: "+82 10-1234-5678" -> "01012345678"
     * @param phoneNumber 카카오 API에서 받은 전화번호
     * @return 변환된 전화번호 (숫자만)
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        
        // 국제전화번호 형식 처리
        // "+82 10-1234-5678" -> "01012345678"
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        
        // +82로 시작하는 경우 한국 번호로 변환
        if (cleaned.startsWith("82")) {
            // 821012345678 -> 01012345678
            if (cleaned.length() == 12) {
                return "0" + cleaned.substring(2);
            }
        }
        
        // 이미 010으로 시작하는 경우 그대로 반환
        if (cleaned.startsWith("010") && cleaned.length() == 11) {
            return cleaned;
        }
        
        // 다른 형식은 그대로 반환
        return cleaned;
    }
}
