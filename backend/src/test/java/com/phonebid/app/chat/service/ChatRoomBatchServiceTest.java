package com.phonebid.app.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.phonebid.app.auction.domain.ActivationMethod;
import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PurchaseMethod;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.phone.domain.Brand;
import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatRoomBatchServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomDeletionService chatRoomDeletionService;

    @InjectMocks
    private ChatRoomBatchService chatRoomBatchService;

    @Test
    @DisplayName("양쪽 모두 삭제한 채팅방이 정리되어야 함")
    void cleanupDeletedChatRooms_shouldDeleteFullyDeletedRooms() {
        // given
        UUID chatRoomId1 = UUID.randomUUID();
        UUID chatRoomId2 = UUID.randomUUID();
        
        ChatRoom room1 = createChatRoom(chatRoomId1);
        ChatRoom room2 = createChatRoom(chatRoomId2);
        
        List<ChatRoom> fullyDeletedRooms = Arrays.asList(room1, room2);

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(fullyDeletedRooms);
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room1)).thenReturn(5);
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room2)).thenReturn(3);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService, times(2)).deleteChatRoomInTransaction(any(ChatRoom.class));
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room1);
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room2);
    }

    @Test
    @DisplayName("정리할 채팅방이 없으면 아무것도 삭제하지 않아야 함")
    void cleanupDeletedChatRooms_shouldDoNothingWhenNoRoomsToClean() {
        // given
        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(Collections.emptyList());

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService, never()).deleteChatRoomInTransaction(any(ChatRoom.class));
    }

    @Test
    @DisplayName("한 채팅방 삭제 실패 시 다른 채팅방은 계속 삭제되어야 함")
    void cleanupDeletedChatRooms_shouldContinueWhenOneRoomFails() {
        // given
        UUID chatRoomId1 = UUID.randomUUID();
        UUID chatRoomId2 = UUID.randomUUID();
        
        ChatRoom room1 = createChatRoom(chatRoomId1);
        ChatRoom room2 = createChatRoom(chatRoomId2);
        
        List<ChatRoom> fullyDeletedRooms = Arrays.asList(room1, room2);

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(fullyDeletedRooms);
        
        // room1 삭제 시 예외 발생
        doThrow(new RuntimeException("DB 오류")).when(chatRoomDeletionService).deleteChatRoomInTransaction(room1);
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room2)).thenReturn(1);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        // room1은 실패했지만 room2는 성공해야 함
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room1);
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room2);
    }

    @Test
    @DisplayName("cleanupDeletedChatRooms는 ChatRoomDeletionService를 통해 채팅방을 삭제해야 함")
    void cleanupDeletedChatRooms_shouldCallDeletionService() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        int messageCount = 10;

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(List.of(room));
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room)).thenReturn(messageCount);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room);
    }

    @Test
    @DisplayName("메시지가 없는 채팅방도 정상적으로 삭제되어야 함")
    void cleanupDeletedChatRooms_shouldDeleteRoomWithNoMessages() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        
        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(List.of(room));
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room)).thenReturn(0);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room);
    }


    @Test
    @DisplayName("findFullyDeletedChatRooms 쿼리 예외 발생 시 전체 작업이 중단되어야 함")
    void cleanupDeletedChatRooms_shouldThrowWhenQueryFails() {
        // given
        when(chatRoomRepository.findFullyDeletedChatRooms())
                .thenThrow(new RuntimeException("DB 연결 오류"));

        // when & then
        try {
            chatRoomBatchService.cleanupDeletedChatRooms();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("DB 연결 오류");
        }

        verify(chatRoomDeletionService, never()).deleteChatRoomInTransaction(any(ChatRoom.class));
    }

    @Test
    @DisplayName("대량의 채팅방(100개 이상)도 정상적으로 처리되어야 함")
    void cleanupDeletedChatRooms_shouldHandleLargeBatch() {
        // given
        List<ChatRoom> largeBatch = java.util.stream.IntStream.range(0, 150)
                .mapToObj(i -> createChatRoom(UUID.randomUUID()))
                .toList();

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(largeBatch);
        when(chatRoomDeletionService.deleteChatRoomInTransaction(any(ChatRoom.class))).thenReturn(5);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService, times(150)).deleteChatRoomInTransaction(any(ChatRoom.class));
    }

    @Test
    @DisplayName("메시지가 매우 많은 채팅방(1000개 이상)도 정상적으로 삭제되어야 함")
    void cleanupDeletedChatRooms_shouldDeleteRoomWithManyMessages() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        int largeMessageCount = 1500;

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(List.of(room));
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room)).thenReturn(largeMessageCount);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room);
    }

    @Test
    @DisplayName("ChatRoomDeletionService에서 예외 발생 시 cleanupDeletedChatRooms가 예외를 처리해야 함")
    void cleanupDeletedChatRooms_shouldHandleExceptionFromDeletionService() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(List.of(room));
        doThrow(new RuntimeException("ChatRoom 삭제 실패")).when(chatRoomDeletionService).deleteChatRoomInTransaction(room);

        // when - 예외가 발생하지 않아야 함 (내부에서 처리)
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room);
    }

    @Test
    @DisplayName("모든 채팅방 삭제가 실패해도 예외를 던지지 않고 로그만 남겨야 함")
    void cleanupDeletedChatRooms_shouldNotThrowWhenAllRoomsFail() {
        // given
        UUID chatRoomId1 = UUID.randomUUID();
        UUID chatRoomId2 = UUID.randomUUID();
        
        ChatRoom room1 = createChatRoom(chatRoomId1);
        ChatRoom room2 = createChatRoom(chatRoomId2);
        
        List<ChatRoom> fullyDeletedRooms = Arrays.asList(room1, room2);

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(fullyDeletedRooms);
        
        // 모든 채팅방 삭제 시 예외 발생
        doThrow(new RuntimeException("메시지 삭제 실패")).when(chatRoomDeletionService).deleteChatRoomInTransaction(any(ChatRoom.class));

        // when - 예외가 발생하지 않아야 함
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService, times(2)).deleteChatRoomInTransaction(any(ChatRoom.class));
    }

    @Test
    @DisplayName("cleanupDeletedChatRooms는 ChatRoomDeletionService로부터 정확한 메시지 수를 받아야 함")
    void cleanupDeletedChatRooms_shouldReceiveCorrectMessageCount() {
        // given
        UUID chatRoomId = UUID.randomUUID();
        ChatRoom room = createChatRoom(chatRoomId);
        int expectedMessageCount = 100;

        when(chatRoomRepository.findFullyDeletedChatRooms()).thenReturn(List.of(room));
        when(chatRoomDeletionService.deleteChatRoomInTransaction(room)).thenReturn(expectedMessageCount);

        // when
        chatRoomBatchService.cleanupDeletedChatRooms();

        // then
        verify(chatRoomDeletionService).deleteChatRoomInTransaction(room);
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

