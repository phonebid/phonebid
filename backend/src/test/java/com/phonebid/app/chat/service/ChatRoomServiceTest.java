package com.phonebid.app.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.chat.domain.ChatMessage;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.dto.request.ChatMessageReadRequest;
import com.phonebid.app.chat.dto.request.ChatRoomCreateRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatRoomResponse;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.chat.repository.UserChatRoomRepository;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.SellerRepository;
import com.phonebid.app.member.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_success() {
        UUID quoteId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID chatRoomId = UUID.randomUUID();

        User consumer = createUser(consumerId, Role.CONSUMER);
        Seller seller = createSeller(sellerId);
        Quote quote = createQuote(quoteId, consumer);

        ChatRoomCreateRequest request = new ChatRoomCreateRequest();
        ReflectionTestUtils.setField(request, "quoteId", quoteId);
        ReflectionTestUtils.setField(request, "consumerId", consumerId);
        ReflectionTestUtils.setField(request, "sellerId", sellerId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(consumerId)).thenReturn(Optional.of(consumer));
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(chatRoomRepository.findByQuoteId(quoteId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", chatRoomId);
            return saved;
        });
        when(userChatRoomRepository.save(any(com.phonebid.app.chat.domain.UserChatRoom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChatRoomResponse response = chatRoomService.createChatRoom(request);

        assertThat(response.getId()).isEqualTo(chatRoomId);
        assertThat(response.getQuoteId()).isEqualTo(quoteId);
        assertThat(response.getConsumerId()).isEqualTo(consumerId);
        assertThat(response.getSellerId()).isEqualTo(seller.getUser().getId());
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(userChatRoomRepository, org.mockito.Mockito.times(2))
                .save(any(com.phonebid.app.chat.domain.UserChatRoom.class));
    }

    @Test
    @DisplayName("이미 존재하는 채팅방이면 예외 발생")
    void createChatRoom_duplicate() {
        UUID quoteId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        User consumer = createUser(consumerId, Role.CONSUMER);
        Seller seller = createSeller(sellerId);
        Quote quote = createQuote(quoteId, consumer);
        ChatRoom existing = ChatRoom.builder()
                .quote(quote)
                .consumer(consumer)
                .seller(seller)
                .build();

        ChatRoomCreateRequest request = new ChatRoomCreateRequest();
        ReflectionTestUtils.setField(request, "quoteId", quoteId);
        ReflectionTestUtils.setField(request, "consumerId", consumerId);
        ReflectionTestUtils.setField(request, "sellerId", sellerId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(consumerId)).thenReturn(Optional.of(consumer));
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(chatRoomRepository.findByQuoteId(quoteId))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> chatRoomService.createChatRoom(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("채팅방 비참여자는 조회 불가")
    void getChatRoom_notParticipant() {
        UUID chatRoomId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User consumer = createUser(UUID.randomUUID(), Role.CONSUMER);
        Seller seller = createSeller(UUID.randomUUID());
        Quote quote = createQuote(UUID.randomUUID(), consumer);

        ChatRoom chatRoom = ChatRoom.builder()
                .quote(quote)
                .consumer(consumer)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userChatRoomRepository.existsActiveByUserIdAndChatRoomId(requesterId, chatRoomId))
                .thenReturn(false);

        assertThatThrownBy(() -> chatRoomService.getChatRoom(chatRoomId, requesterId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("채팅 메시지 읽음 처리")
    void markMessagesAsRead_updatesFlag() {
        UUID chatRoomId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        User consumer = createUser(consumerId, Role.CONSUMER);
        Seller seller = createSeller(UUID.randomUUID());
        Quote quote = createQuote(UUID.randomUUID(), consumer);

        ChatRoom chatRoom = ChatRoom.builder()
                .quote(quote)
                .consumer(consumer)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(consumer)
                .messageType(com.phonebid.app.chat.domain.MessageType.TEXT)
                .content("hello")
                .build();
        ReflectionTestUtils.setField(message, "id", messageId);

        ChatMessageReadRequest request = new ChatMessageReadRequest();
        ReflectionTestUtils.setField(request, "chatRoomId", chatRoomId);
        ReflectionTestUtils.setField(request, "messageIds", List.of(messageId));

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userChatRoomRepository.existsActiveByUserIdAndChatRoomId(consumerId, chatRoomId))
                .thenReturn(true);
        when(chatMessageRepository.findByChatRoomIdAndIdIn(eq(chatRoomId), any())).thenReturn(List.of(message));
        when(chatMessageRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        chatRoomService.markMessagesAsRead(chatRoomId, consumerId, request);

        assertThat(message.isRead()).isTrue();
        verify(chatMessageRepository).findByChatRoomIdAndIdIn(eq(chatRoomId), any());
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 성공")
    void getChatMessages_success() {
        UUID chatRoomId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();

        User consumer = createUser(consumerId, Role.CONSUMER);
        Seller seller = createSeller(UUID.randomUUID());
        Quote quote = createQuote(UUID.randomUUID(), consumer);

        ChatRoom chatRoom = ChatRoom.builder()
                .quote(quote)
                .consumer(consumer)
                .seller(seller)
                .build();

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(consumer)
                .messageType(com.phonebid.app.chat.domain.MessageType.TEXT)
                .content("hello")
                .build();

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userChatRoomRepository.existsActiveByUserIdAndChatRoomId(consumerId, chatRoomId))
                .thenReturn(true);
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)).thenReturn(List.of(message));

        List<ChatMessageResponse> responses = chatRoomService.getChatMessages(chatRoomId, consumerId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContent()).isEqualTo("hello");
    }

    @Test
    @DisplayName("소비자 역할이 아니면 채팅방 생성 불가")
    void createChatRoom_invalidConsumerRole() {
        UUID quoteId = UUID.randomUUID();
        UUID consumerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        User consumer = createUser(consumerId, Role.SELLER);
        Seller seller = createSeller(sellerId);
        Quote quote = createQuote(quoteId, consumer);

        ChatRoomCreateRequest request = new ChatRoomCreateRequest();
        ReflectionTestUtils.setField(request, "quoteId", quoteId);
        ReflectionTestUtils.setField(request, "consumerId", consumerId);
        ReflectionTestUtils.setField(request, "sellerId", sellerId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(consumerId)).thenReturn(Optional.of(consumer));
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(chatRoomRepository.findByQuoteId(quoteId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.createChatRoom(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED.getMessage());
    }

    private Quote createQuote(UUID id, User user) {
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
                .expiredAt(LocalDateTime.now().plusDays(1))
                .purchaseMethod(PurchaseMethod.ANY)
                .currentCarrier(Carrier.SKT)
                .activationMethod(ActivationMethod.ANY)
                .build();
        ReflectionTestUtils.setField(quote, "id", id);
        return quote;
    }

    private User createUser(UUID id, Role role) {
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

    private Seller createSeller(UUID id) {
        Seller seller = Seller.builder()
                .user(createUser(UUID.randomUUID(), Role.SELLER))
                .businessNumber("123-45-67890")
                .storeName("Test Store")
                .storeAddress(null)
                .build();
        seller.approve();
        ReflectionTestUtils.setField(seller, "sellerId", id);
        return seller;
    }
}


