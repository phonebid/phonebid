package com.phonebid.app.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.chat.repository.UserChatRoomRepository;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatRoomDeletionServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomDeletionService chatRoomDeletionService;

    @Test
    @DisplayName("deleteChatRoomInTransaction은 메시지, UserChatRoom, ChatRoom을 순서대로 삭제해야 함")
    void deleteChatRoomInTransaction_shouldDeleteInCorrectOrder() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        int messageCount = 10;

        when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(messageCount);

        // when
        int result = chatRoomDeletionService.deleteChatRoomInTransaction(room);

        // then
        assertThat(result).isEqualTo(messageCount);
        
        InOrder inOrder = inOrder(chatMessageRepository, userChatRoomRepository, chatRoomRepository);
        inOrder.verify(chatMessageRepository).countByChatRoomId(chatRoomId);
        inOrder.verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
        inOrder.verify(userChatRoomRepository).deleteByChatRoomId(chatRoomId);
        inOrder.verify(chatRoomRepository).delete(room);
    }

    @Test
    @DisplayName("deleteChatRoomInTransaction은 정확한 메시지 수를 반환해야 함")
    void deleteChatRoomInTransaction_shouldReturnCorrectMessageCount() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        int[] messageCounts = {0, 1, 10, 100, 1000};

        for (int expectedCount : messageCounts) {
            when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(expectedCount);

            // when
            int result = chatRoomDeletionService.deleteChatRoomInTransaction(room);

            // then
            assertThat(result).isEqualTo(expectedCount);
        }
    }

    @Test
    @DisplayName("메시지가 없는 채팅방도 정상적으로 삭제되어야 함")
    void deleteChatRoomInTransaction_shouldDeleteRoomWithNoMessages() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);

        when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(0);

        // when
        int result = chatRoomDeletionService.deleteChatRoomInTransaction(room);

        // then
        assertThat(result).isEqualTo(0);
        verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
        verify(userChatRoomRepository).deleteByChatRoomId(chatRoomId);
        verify(chatRoomRepository).delete(room);
    }

    @Test
    @DisplayName("메시지가 매우 많은 채팅방(1000개 이상)도 정상적으로 삭제되어야 함")
    void deleteChatRoomInTransaction_shouldDeleteRoomWithManyMessages() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        int largeMessageCount = 1500;

        when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(largeMessageCount);

        // when
        int result = chatRoomDeletionService.deleteChatRoomInTransaction(room);

        // then
        assertThat(result).isEqualTo(largeMessageCount);
        verify(chatMessageRepository).countByChatRoomId(chatRoomId);
        verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
        verify(userChatRoomRepository).deleteByChatRoomId(chatRoomId);
        verify(chatRoomRepository).delete(room);
    }

    @Test
    @DisplayName("메시지 수 조회 단계에서 예외 발생 시 예외가 전파되어야 함")
    void deleteChatRoomInTransaction_shouldPropagateExceptionOnMessageCount() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);

        when(chatMessageRepository.countByChatRoomId(chatRoomId))
                .thenThrow(new RuntimeException("메시지 수 조회 실패"));

        // when & then
        assertThatThrownBy(() -> chatRoomDeletionService.deleteChatRoomInTransaction(room))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("메시지 수 조회 실패");

        verify(chatMessageRepository).countByChatRoomId(chatRoomId);
        verify(chatMessageRepository, never()).deleteByChatRoomId(any(UUID.class));
        verify(userChatRoomRepository, never()).deleteByChatRoomId(any(UUID.class));
        verify(chatRoomRepository, never()).delete(any(ChatRoom.class));
    }

    @Test
    @DisplayName("메시지 삭제 단계에서 예외 발생 시 예외가 전파되어야 함")
    void deleteChatRoomInTransaction_shouldPropagateExceptionOnMessageDelete() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);

        when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(5);
        doThrow(new RuntimeException("메시지 삭제 실패"))
                .when(chatMessageRepository).deleteByChatRoomId(chatRoomId);

        // when & then
        assertThatThrownBy(() -> chatRoomDeletionService.deleteChatRoomInTransaction(room))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("메시지 삭제 실패");

        verify(chatMessageRepository).countByChatRoomId(chatRoomId);
        verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
        verify(userChatRoomRepository, never()).deleteByChatRoomId(any(UUID.class));
        verify(chatRoomRepository, never()).delete(any(ChatRoom.class));
    }

    @Test
    @DisplayName("UserChatRoom 삭제 단계에서 예외 발생 시 예외가 전파되어야 함")
    void deleteChatRoomInTransaction_shouldPropagateExceptionOnUserChatRoomDelete() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);

        when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(5);
        doThrow(new RuntimeException("UserChatRoom 삭제 실패"))
                .when(userChatRoomRepository).deleteByChatRoomId(chatRoomId);

        // when & then
        assertThatThrownBy(() -> chatRoomDeletionService.deleteChatRoomInTransaction(room))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UserChatRoom 삭제 실패");

        verify(chatMessageRepository).countByChatRoomId(chatRoomId);
        verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
        verify(userChatRoomRepository).deleteByChatRoomId(chatRoomId);
        verify(chatRoomRepository, never()).delete(room);
    }

    @Test
    @DisplayName("ChatRoom 삭제 단계에서 예외 발생 시 예외가 전파되어야 함")
    void deleteChatRoomInTransaction_shouldPropagateExceptionOnChatRoomDelete() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);

        when(chatMessageRepository.countByChatRoomId(chatRoomId)).thenReturn(5);
        doThrow(new RuntimeException("ChatRoom 삭제 실패"))
                .when(chatRoomRepository).delete(room);

        // when & then
        assertThatThrownBy(() -> chatRoomDeletionService.deleteChatRoomInTransaction(room))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ChatRoom 삭제 실패");

        verify(chatMessageRepository).countByChatRoomId(chatRoomId);
        verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
        verify(userChatRoomRepository).deleteByChatRoomId(chatRoomId);
        verify(chatRoomRepository).delete(room);
    }

    private ChatRoom createChatRoom(UUID id) {
        User consumer = createUser(UUID.randomUUID(), Role.CONSUMER);
        Seller seller = createSeller(UUID.randomUUID());
        Quote quote = createQuote(UUID.randomUUID(), consumer);

        ChatRoom chatRoom = ChatRoom.builder()
                .quote(quote)
                .consumer(consumer)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", id);
        return chatRoom;
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

