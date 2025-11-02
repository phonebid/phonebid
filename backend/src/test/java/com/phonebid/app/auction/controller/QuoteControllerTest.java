package com.phonebid.app.auction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.domain.QuoteStatus;
import com.phonebid.app.auction.dto.request.QuoteCreateRequestDto;
import com.phonebid.app.auction.dto.response.QuoteResponseDto;
import com.phonebid.app.auction.service.QuoteService;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuoteController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@DisplayName("QuoteController 테스트")
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuoteService quoteService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserDetailsImpl userDetails;
    private Quote testQuote1;
    private Quote testQuote2;
    private QuoteCreateRequestDto quoteCreateRequestDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스트닉네임")
                .role(Role.CONSUMER)
                .build();
        
        // UUID 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set UUID", e);
        }

        userDetails = new UserDetailsImpl(testUser, testUser.getUsername());

        // 테스트 Quote 생성
        testQuote1 = Quote.builder()
                .user(testUser)
                .model("iPhone 16")
                .storage("128GB")
                .carrier(Carrier.SKT)
                .color("블랙")
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .expiredAt(LocalDateTime.now().plusHours(24))
                .build();

        testQuote2 = Quote.builder()
                .user(testUser)
                .model("Galaxy S24")
                .storage("256GB")
                .carrier(Carrier.KT)
                .color("화이트")
                .purchaseMethod(PurchaseMethod.DEVICE_CHANGE)
                .activationMethod(ActivationMethod.COMMON_SUBSIDY)
                .expiredAt(LocalDateTime.now().plusHours(12))
                .build();

        // 테스트 요청 DTO 생성
        quoteCreateRequestDto = QuoteCreateRequestDto.builder()
                .model("iPhone 16")
                .storage("128GB")
                .color("블랙")
                .carrier(Carrier.SKT)
                .purchaseMethod(PurchaseMethod.NEW_SUBSCRIPTION)
                .activationMethod(ActivationMethod.SELECTIVE_SUBSIDY)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    private void setAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("GET /my - 200 OK: 인증된 사용자의 Quote 조회")
    void getMyOpenQuotes_Success() throws Exception {
        // given
        setAuthentication();
        List<QuoteResponseDto> mockResponse = Arrays.asList(
                QuoteResponseDto.from(testQuote1),
                QuoteResponseDto.from(testQuote2)
        );
        when(quoteService.getMyOpenQuotes(eq(testUser), any(Pageable.class)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/auction/quotes/my")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("내 견적 조회가 성공적으로 완료되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].model").value("iPhone 16"))
                .andExpect(jsonPath("$.data[1].model").value("Galaxy S24"));
    }

    // 인증 관련 테스트는 Spring Security가 활성화된 환경에서만 의미가 있으므로 제외

    @Test
    @DisplayName("GET / - 200 OK: 모든 OPEN Quote 조회")
    void getAllOpenQuotes_Success() throws Exception {
        // given
        List<QuoteResponseDto> mockResponse = Arrays.asList(
                QuoteResponseDto.from(testQuote1),
                QuoteResponseDto.from(testQuote2)
        );
        when(quoteService.getAllOpenQuotes(any(Pageable.class)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/auction/quotes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("진행중인 견적 조회가 성공적으로 완료되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].model").value("iPhone 16"))
                .andExpect(jsonPath("$.data[1].model").value("Galaxy S24"));
    }

    @Test
    @DisplayName("POST / - 200 OK: Quote 생성 성공")
    void createQuote_Success() throws Exception {
        // given
        setAuthentication();
        doNothing().when(quoteService).createQuote(any(QuoteCreateRequestDto.class), eq(testUser));

        // when & then
        mockMvc.perform(post("/api/v1/auction/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quoteCreateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("견적 생성이 성공적으로 완료되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
