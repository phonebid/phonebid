package com.phonebid.app.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.phonebid.app.chat.domain.ChatMessage;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.domain.MessageType;
import com.phonebid.app.chat.dto.request.ChatMessageSendRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    @DisplayName("참여자가 메시지를 전송하면 저장된다")
    void sendMessage_success() {
        UUID chatRoomId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String username = "testuser";

        User consumer = createUser(senderId, Role.CONSUMER);
        Seller seller = createSeller();
        ChatRoom chatRoom = ChatRoom.builder()
                .quote(null)
                .consumer(consumer)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        ChatMessageSendRequest request = new ChatMessageSendRequest();
        ReflectionTestUtils.setField(request, "chatRoomId", chatRoomId);
        ReflectionTestUtils.setField(request, "senderId", username);
        ReflectionTestUtils.setField(request, "messageType", MessageType.TEXT);
        ReflectionTestUtils.setField(request, "content", "hello");

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(consumer));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        ChatMessageResponse response = chatMessageService.sendMessage(request);

        assertThat(response.getContent()).isEqualTo("hello");
        assertThat(response.getMessageType()).isEqualTo(MessageType.TEXT);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("비참여자가 메시지를 전송하면 예외 발생")
    void sendMessage_notParticipant() {
        UUID chatRoomId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String username = "testuser";

        User consumer = createUser(UUID.randomUUID(), Role.CONSUMER);
        Seller seller = createSeller();
        ChatRoom chatRoom = ChatRoom.builder()
                .quote(null)
                .consumer(consumer)
                .seller(seller)
                .build();

        ChatMessageSendRequest request = new ChatMessageSendRequest();
        ReflectionTestUtils.setField(request, "chatRoomId", chatRoomId);
        ReflectionTestUtils.setField(request, "senderId", username);
        ReflectionTestUtils.setField(request, "messageType", MessageType.TEXT);
        ReflectionTestUtils.setField(request, "content", "hi");

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(createUser(senderId, Role.CONSUMER)));

        assertThatThrownBy(() -> chatMessageService.sendMessage(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 발신자이면 예외")
    void sendMessage_userNotFound() {
        UUID chatRoomId = UUID.randomUUID();
        String username = "nonexistent";

        User consumer = createUser(UUID.randomUUID(), Role.CONSUMER);
        Seller seller = createSeller();
        ChatRoom chatRoom = ChatRoom.builder()
                .quote(null)
                .consumer(consumer)
                .seller(seller)
                .build();

        ChatMessageSendRequest request = new ChatMessageSendRequest();
        ReflectionTestUtils.setField(request, "chatRoomId", chatRoomId);
        ReflectionTestUtils.setField(request, "senderId", username);
        ReflectionTestUtils.setField(request, "messageType", MessageType.TEXT);
        ReflectionTestUtils.setField(request, "content", "hi");

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatMessageService.sendMessage(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.USER_NOT_FOUND.getMessage());
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

    private Seller createSeller() {
        Seller seller = Seller.builder()
                .user(createUser(UUID.randomUUID(), Role.SELLER))
                .businessNumber("123-45-67890")
                .storeName("Store")
                .storeAddress(null)
                .build();
        seller.approve();
        ReflectionTestUtils.setField(seller, "sellerId", UUID.randomUUID());
        return seller;
    }
}


