package com.phonebid.app.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.chat.controller.ChatRestController;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.domain.MessageType;
import com.phonebid.app.chat.dto.request.ChatMessageReadRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatRoomResponse;
import com.phonebid.app.chat.service.ChatRoomService;
import com.phonebid.app.common.exception.GlobalExceptionHandler;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.security.UserDetailsImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ChatRestControllerTest {

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatRestController chatRestController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("채팅방 생성 API")
    void createChatRoom() throws Exception {
        UUID chatRoomId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        User consumer = TestFixtures.user(consumerId, Role.CONSUMER);
        ChatRoomResponse response = TestFixtures.chatRoomResponse(chatRoomId, quoteId, consumerId, sellerId);

        when(chatRoomService.createChatRoom(any())).thenReturn(response);

        String body = "{" +
                "\"quoteId\":\"" + quoteId + "\"," +
                "\"consumerId\":\"" + consumerId + "\"," +
                "\"sellerId\":\"" + sellerId + "\"" +
                "}";

        mockMvc.perform(post("/api/v1/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(auth(consumer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(chatRoomId.toString()))
                .andExpect(jsonPath("$.data.quoteId").value(quoteId.toString()));

        verify(chatRoomService, times(1)).createChatRoom(any());
    }

    @Test
    @DisplayName("채팅방 생성 API - consumerId 불일치 시 403")
    void createChatRoom_consumerIdMismatch() throws Exception {
        UUID quoteId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        UUID requestConsumerId = UUID.randomUUID(); // 다른 사용자 ID
        UUID sellerId = UUID.randomUUID();

        User authenticatedUser = TestFixtures.user(authenticatedUserId, Role.CONSUMER);

        String body = "{" +
                "\"quoteId\":\"" + quoteId + "\"," +
                "\"consumerId\":\"" + requestConsumerId + "\"," +
                "\"sellerId\":\"" + sellerId + "\"" +
                "}";

        mockMvc.perform(post("/api/v1/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(auth(authenticatedUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("채팅방 목록 조회 API (페이징)")
    void getChatRooms() throws Exception {
        UUID consumerId = UUID.randomUUID();
        UUID quoteId1 = UUID.randomUUID();
        UUID quoteId2 = UUID.randomUUID();
        UUID sellerId1 = UUID.randomUUID();
        UUID sellerId2 = UUID.randomUUID();
        UUID chatRoomId1 = UUID.randomUUID();
        UUID chatRoomId2 = UUID.randomUUID();

        User consumer = TestFixtures.user(consumerId, Role.CONSUMER);
        
        ChatRoomResponse response1 = TestFixtures.chatRoomResponse(chatRoomId1, quoteId1, consumerId, sellerId1);
        ChatRoomResponse response2 = TestFixtures.chatRoomResponse(chatRoomId2, quoteId2, consumerId, sellerId2);
        
        Page<ChatRoomResponse> page = new PageImpl<>(
            List.of(response1, response2),
            PageRequest.of(0, 20),
            2
        );
        
        when(chatRoomService.getChatRoomsByUser(eq(consumerId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/chat/rooms")
                        .param("page", "0")
                        .param("size", "20")
                        .with(auth(consumer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(chatRoomId1.toString()))
                .andExpect(jsonPath("$.data.content[1].id").value(chatRoomId2.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(20));

        verify(chatRoomService).getChatRoomsByUser(eq(consumerId), any(Pageable.class));
    }

    @Test
    @DisplayName("채팅방 상세 조회 API")
    void getChatRoom() throws Exception {
        UUID chatRoomId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        User consumer = TestFixtures.user(consumerId, Role.CONSUMER);
        ChatRoomResponse response = TestFixtures.chatRoomResponse(chatRoomId, quoteId, consumerId, sellerId);
        when(chatRoomService.getChatRoom(chatRoomId, consumerId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/chat/rooms/{chatRoomId}", chatRoomId)
                        .with(auth(consumer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(chatRoomId.toString()));

        verify(chatRoomService).getChatRoom(chatRoomId, consumerId);
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 API")
    void getMessages() throws Exception {
        UUID chatRoomId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();

        User consumer = TestFixtures.user(consumerId, Role.CONSUMER);
        when(chatRoomService.getChatMessages(chatRoomId, consumerId))
                .thenReturn(List.of(TestFixtures.chatMessageResponse(chatRoomId, consumerId, "hi")));

        mockMvc.perform(get("/api/v1/chat/rooms/{chatRoomId}/messages", chatRoomId)
                        .with(auth(consumer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("hi"));

        verify(chatRoomService).getChatMessages(chatRoomId, consumerId);
    }

    @Test
    @DisplayName("채팅 메시지 읽음 처리 API")
    void markMessagesAsRead() throws Exception {
        UUID chatRoomId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();

        String body = objectMapper.writeValueAsString(new ReadRequestFixture(chatRoomId));

        User consumer = TestFixtures.user(consumerId, Role.CONSUMER);

        mockMvc.perform(post("/api/v1/chat/rooms/{chatRoomId}/messages/read", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                .with(auth(consumer)))
                .andExpect(status().isOk());

        ArgumentCaptor<ChatMessageReadRequest> captor = ArgumentCaptor.forClass(ChatMessageReadRequest.class);
        verify(chatRoomService).markMessagesAsRead(eq(chatRoomId), eq(consumerId), captor.capture());
        assertThat(captor.getValue().getMessageIds()).isNotEmpty();
    }

    private RequestPostProcessor auth(User user) {
        return request -> {
            UserDetailsImpl userDetails = new UserDetailsImpl(user, user.getUsername());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            request.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            
            return request;
        };
    }

    private static class ReadRequestFixture {
        private final UUID chatRoomId;
        private final List<UUID> messageIds;

        private ReadRequestFixture(UUID chatRoomId) {
            this.chatRoomId = chatRoomId;
            this.messageIds = List.of(UUID.randomUUID());
        }

        public UUID getChatRoomId() {
            return chatRoomId;
        }

        public List<UUID> getMessageIds() {
            return messageIds;
        }
    }

    private static class TestFixtures {

        private static User user(UUID id, Role role) {
            User user = User.builder()
                    .username("user" + id.toString().substring(0, 5))
                    .password("password")
                    .email(id + "@test.com")
                    .name("User")
                    .nickname("nick")
                    .phone("01012345678")
                    .role(role)
                    .provider(null)
                    .providerId(null)
                    .build();
            ReflectionTestUtils.setField(user, "id", id);
            return user;
        }

        private static com.phonebid.app.member.domain.Seller seller(UUID id) {
            com.phonebid.app.member.domain.Seller seller = com.phonebid.app.member.domain.Seller.builder()
                    .user(user(UUID.randomUUID(), Role.SELLER))
                    .businessNumber("123-45-67890")
                    .storeName("Store")
                    .storeAddress(null)
                    .build();
            seller.approve();
            ReflectionTestUtils.setField(seller, "sellerId", id);
            return seller;
        }

        private static Quote quote(UUID id, User user) {
            PhoneModel phoneModel = PhoneModel.builder()
                    .brand(Brand.APPLE)
                    .model("iPhone 16")
                    .build();
            ReflectionTestUtils.setField(phoneModel, "id", UUID.randomUUID());
            
            PhoneOption storageOption = PhoneOption.builder()
                    .model(phoneModel)
                    .optionType(PhoneOption.OptionType.STORAGE)
                    .optionValue("128")
                    .displayLabel("128GB")
                    .build();
            ReflectionTestUtils.setField(storageOption, "id", UUID.randomUUID());
            
            PhoneOption colorOption = PhoneOption.builder()
                    .model(phoneModel)
                    .optionType(PhoneOption.OptionType.COLOR)
                    .optionValue("BLACK")
                    .displayLabel("블랙")
                    .build();
            ReflectionTestUtils.setField(colorOption, "id", UUID.randomUUID());
            
            Quote quote = Quote.builder()
                    .user(user)
                    .phoneModel(phoneModel)
                    .storage(storageOption)
                    .carrier(Carrier.SKT)
                    .color(colorOption)
                    .expiredAt(java.time.LocalDateTime.now().plusDays(1))
                    .purchaseMethod(PurchaseMethod.ANY)
                    .currentCarrier(Carrier.SKT)
                    .activationMethod(ActivationMethod.ANY)
                    .build();
            ReflectionTestUtils.setField(quote, "id", id);
            return quote;
        }

        private static ChatRoom chatRoom(UUID chatRoomId, UUID quoteId, UUID consumerId, UUID sellerId) {
            User consumer = user(consumerId, Role.CONSUMER);
            com.phonebid.app.member.domain.Seller seller = seller(sellerId);
            Quote quote = quote(quoteId, consumer);
            ChatRoom room = ChatRoom.builder()
                    .quote(quote)
                    .consumer(consumer)
                    .seller(seller)
                    .build();
            ReflectionTestUtils.setField(room, "id", chatRoomId);
            return room;
        }

        private static ChatRoomResponse chatRoomResponse(UUID chatRoomId, UUID quoteId, UUID consumerId, UUID sellerId) {
            ChatRoomResponse response = new ChatRoomResponse();
            ReflectionTestUtils.setField(response, "id", chatRoomId);
            ReflectionTestUtils.setField(response, "quoteId", quoteId);
            ReflectionTestUtils.setField(response, "consumerId", consumerId);
            ReflectionTestUtils.setField(response, "sellerId", sellerId);
            ReflectionTestUtils.setField(response, "status", com.phonebid.app.chat.domain.ChatRoomStatus.ACTIVE);
            return response;
        }

        private static ChatMessageResponse chatMessageResponse(UUID chatRoomId, UUID senderId, String content) {
            ChatMessageResponse response = new ChatMessageResponse();
            ReflectionTestUtils.setField(response, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(response, "chatRoomId", chatRoomId);
            ReflectionTestUtils.setField(response, "senderId", senderId);
            ReflectionTestUtils.setField(response, "messageType", MessageType.TEXT);
            ReflectionTestUtils.setField(response, "content", content);
            ReflectionTestUtils.setField(response, "isRead", false);
            return response;
        }
    }
}


