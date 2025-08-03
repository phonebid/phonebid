package com.phonebid.app.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.exception.KakaoErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.member.domain.Provider;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.ResponseDto.KakaoUserInfoDto;
import com.phonebid.app.member.dto.ResponseDto.LoginResponseDto;
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
     * 카카오 사용자 회원가입 처리
     * @param kakaoUserInfo 카카오 사용자 정보
     * @return 등록된 사용자 엔티티
     */
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
                // 신규 회원가입
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);
                
                // username은 email 기반으로 생성 (중복 방지)
                String username = generateUsername(kakaoUserInfo.getEmail());
                
                kakaoUser = User.builder()
                        .username(username)
                        .password(encodedPassword)
                        .email(kakaoUserInfo.getEmail())
                        .name(kakaoUserInfo.getName())
                        .nickname(kakaoUserInfo.getNickname())
                        .role(Role.CONSUMER) // 기본값은 소비자
                        .provider(Provider.KAKAO)
                        .providerId(providerId)
                        .build();
                
                log.info("카카오 신규 사용자 등록: username={}, email={}", username, kakaoUserInfo.getEmail());
            }
            
            userRepository.save(kakaoUser);
        }
        
        return kakaoUser;
    }
    
    /**
     * 이메일 기반으로 고유한 username 생성
     * @param email 이메일
     * @return 고유한 username
     */
    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int suffix = 1;
        
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix;
            suffix++;
        }
        
        return username;
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
            
            Long id = jsonNode.get("id").asLong();
            String nickname = jsonNode.get("properties")
                    .get("nickname").asText();
            String email = jsonNode.get("kakao_account")
                    .get("email").asText();
            String name = jsonNode.get("kakao_account")
                    .get("name") != null ? jsonNode.get("kakao_account").get("name").asText() : nickname;
            
            log.info("카카오 사용자 정보: id={}, nickname={}, email={}", id, nickname, email);
            return KakaoUserInfoDto.of(id, nickname, email, name);
        } catch (JsonProcessingException e) {
            log.error("카카오 사용자 정보 응답 파싱 실패", e);
            throw new CustomException(KakaoErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
        }
    }
}
