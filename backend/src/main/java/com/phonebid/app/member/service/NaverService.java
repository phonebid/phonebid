package com.phonebid.app.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.NaverErrorCode;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.auth.service.RefreshTokenService;
import com.phonebid.app.member.domain.Provider;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.dto.response.NaverUserInfoDto;
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
import java.util.Optional;

/**
 * 네이버 OAuth2 인증 서비스
 * 네이버 로그인 및 사용자 정보 처리를 담당하는 클래스
 */
@Slf4j(topic = "NAVER Login")
@Service
@RequiredArgsConstructor
public class NaverService {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    
    @Value("${oauth.naver.client-id}")
    private String clientId;
    
    @Value("${oauth.naver.client-secret}")
    private String clientSecret;
    
    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;
    
    /**
     * 네이버 로그인 처리
     * @param code 네이버 인가 코드
     * @return 로그인 응답 DTO
     * @throws CustomException OAuth2 처리 중 오류 발생 시
     */
    @Transactional
    public LoginResponseDto naverLogin(String code) throws CustomException {
        try {
            // 1. 인가 코드로 네이버 액세스 토큰 요청
            String naverAccessToken = getToken(code);
            
            // 2. 네이버 액세스 토큰으로 네이버 사용자 정보 가져오기
            NaverUserInfoDto naverUserInfo = getNaverUserInfo(naverAccessToken);
            
            // 3. 필요시 회원가입 처리
            User naverUser = registerNaverUserIfNeeded(naverUserInfo);
            
            // 4. Refresh Token 생성 및 저장
            refreshTokenService.deleteByUserId(naverUser.getId());
            String refreshToken = refreshTokenService.createRefreshToken(naverUser.getId());
            
            // 5. JWT Access Token 생성 및 반환
            String accessToken = jwtUtil.createToken(naverUser.getUsername(), naverUser.getRole(), false);
            
            // 6. DTO에 RefreshToken 포함하여 반환
            return LoginResponseDto.of(accessToken, refreshToken, naverUser.getUsername(), naverUser.getNickname(), naverUser.getRole().name());
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 예상치 못한 오류 발생", e);
            throw new CustomException(NaverErrorCode.NAVER_LOGIN_PROCESSING_FAILED);
        }
    }
    
    /**
     * 네이버 사용자 정보로 사용자 등록 또는 연동
     * @param naverUserInfo 네이버 사용자 정보
     * @return 등록된 사용자 엔티티
     */
    @Transactional
    private User registerNaverUserIfNeeded(NaverUserInfoDto naverUserInfo) {
        // DB에 중복된 네이버 providerId가 있는지 확인
        String providerId = naverUserInfo.getId();
        User naverUser = userRepository.findByProviderId(providerId).orElse(null);
        
        if (naverUser == null) {
            // 네이버 사용자 email과 동일한 email 가진 회원이 있는지 확인
            String naverEmail = naverUserInfo.getEmail();
            User sameEmailUser = userRepository.findByEmail(naverEmail).orElse(null);
            
            if (sameEmailUser != null) {
                // 동일한 이메일 사용자가 있으면 네이버 연동
                naverUser = sameEmailUser;
                naverUser.updateProvider(Provider.NAVER);
                naverUser.updateProviderId(providerId);
                log.info("기존 사용자에 네이버 연동 완료: username={}", naverUser.getUsername());
            } else {
                // 신규 회원가입 - 네이버 이메일을 그대로 username으로 사용
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);
                
                // username은 네이버 이메일 그대로 사용
                String username = naverUserInfo.getEmail();
                
                // username 중복 체크
                Optional<User> existingUser = userRepository.findByUsername(username);
                if (existingUser.isPresent()) {
                    log.error("네이버 로그인 중 username 중복 발생: username={}", username);
                    throw new CustomException(CommonErrorCode.DUPLICATE_USERNAME);
                }
                
                naverUser = User.builder()
                        .username(username)
                        .password(encodedPassword)
                        .email(naverUserInfo.getEmail())
                        .name(naverUserInfo.getName())
                        .nickname(naverUserInfo.getNickname() != null ? naverUserInfo.getNickname() : naverUserInfo.getName())
                        .phone(naverUserInfo.getPhone())
                        .role(Role.CONSUMER) // 기본값은 소비자
                        .provider(Provider.NAVER)
                        .providerId(providerId)
                        .build();
                
                log.info("네이버 신규 사용자 등록 완료: username={}", username);
            }
            
            userRepository.save(naverUser);
        }
        
        return naverUser;
    }
    
    /**
     * 인가 코드로 액세스 토큰 요청
     * @param code 네이버 인가 코드
     * @return 액세스 토큰
     * @throws CustomException 토큰 요청 실패 시
     */
    private String getToken(String code) throws CustomException {
        String tokenUrl = UriComponentsBuilder
                .fromUriString("https://nid.naver.com")
                .path("/oauth2.0/token")
                .encode()
                .build()
                .toUriString();
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
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
            throw new CustomException(NaverErrorCode.NAVER_TOKEN_REQUEST_FAILED);
        }
        
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response);
            
            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                String errorDescription = jsonNode.has("error_description") ? 
                    jsonNode.get("error_description").asText() : "알 수 없는 오류";
                log.error("네이버 토큰 요청 실패: {} - {}", error, errorDescription);
                throw new CustomException(NaverErrorCode.NAVER_TOKEN_REQUEST_FAILED);
            }
            
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            log.error("네이버 토큰 응답 파싱 실패", e);
            throw new CustomException(NaverErrorCode.NAVER_TOKEN_REQUEST_FAILED);
        }
    }
    
    /**
     * 액세스 토큰으로 네이버 사용자 정보 가져오기
     * @param accessToken 액세스 토큰
     * @return 네이버 사용자 정보 DTO
     * @throws CustomException 사용자 정보 요청 실패 시
     */
    private NaverUserInfoDto getNaverUserInfo(String accessToken) throws CustomException {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        
        String response = webClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        if (response == null) {
            throw new CustomException(NaverErrorCode.NAVER_USER_INFO_REQUEST_FAILED);
        }
        
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response);
            
            if (jsonNode.has("errorCode")) {
                String errorCode = jsonNode.get("errorCode").asText();
                String errorMessage = jsonNode.has("errorMessage") ? 
                    jsonNode.get("errorMessage").asText() : "알 수 없는 오류";
                log.error("네이버 사용자 정보 요청 실패: {} - {}", errorCode, errorMessage);
                throw new CustomException(NaverErrorCode.NAVER_USER_INFO_REQUEST_FAILED);
            }
            
            // 네이버 API 응답 구조: response -> { id, email, name, mobile, nickname }
            JsonNode responseNode = jsonNode.get("response");
            if (responseNode == null) {
                log.error("네이버 사용자 정보 응답에 response 필드가 없습니다");
                throw new CustomException(NaverErrorCode.NAVER_API_RESPONSE_PARSE_FAILED);
            }
            
            // 필수 필드 검증
            if (!responseNode.has("id")) {
                log.error("네이버 사용자 정보 응답에 id 필드가 없습니다");
                throw new CustomException(NaverErrorCode.NAVER_API_RESPONSE_PARSE_FAILED);
            }
            
            if (!responseNode.has("email")) {
                log.error("네이버 사용자 정보 응답에 email 필드가 없습니다");
                throw new CustomException(NaverErrorCode.NAVER_API_RESPONSE_PARSE_FAILED);
            }
            
            if (!responseNode.has("name")) {
                log.error("네이버 사용자 정보 응답에 name 필드가 없습니다");
                throw new CustomException(NaverErrorCode.NAVER_API_RESPONSE_PARSE_FAILED);
            }
            
            if (!responseNode.has("mobile")) {
                log.error("네이버 사용자 정보 응답에 mobile 필드가 없습니다");
                throw new CustomException(NaverErrorCode.NAVER_API_RESPONSE_PARSE_FAILED);
            }
            
            // 안전한 필드 추출
            String id = responseNode.get("id").asText();
            String email = responseNode.get("email").asText();
            String name = responseNode.get("name").asText();
            String phone = formatPhoneNumber(responseNode.get("mobile").asText());
            String nickname = responseNode.has("nickname") ? responseNode.get("nickname").asText() : name;
            
            log.debug("네이버 사용자 정보 조회 성공: id={}", id);
            return NaverUserInfoDto.of(id, email, name, phone, nickname);
        } catch (JsonProcessingException e) {
            log.error("네이버 사용자 정보 응답 파싱 실패", e);
            throw new CustomException(NaverErrorCode.NAVER_USER_INFO_REQUEST_FAILED);
        }
    }
    
    /**
     * 네이버 API에서 받은 전화번호를 한국 휴대폰 번호 형식으로 변환합니다.
     * 예: "+82 10-1234-5678" -> "01012345678"
     * @param phoneNumber 네이버 API에서 받은 전화번호
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