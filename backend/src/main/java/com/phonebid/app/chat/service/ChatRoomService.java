package com.phonebid.app.chat.service;

import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.dto.request.ChatMessageReadRequest;
import com.phonebid.app.chat.dto.request.ChatRoomCreateRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatRoomResponse;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.SellerRepository;
import com.phonebid.app.member.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅방 도메인의 비즈니스 로직을 담당하는 클래스
 * 채팅방 생성, 중복 방 체크, 참여자 권한 검증, 메시지 조회/읽음 처리 등
 * "채팅방 단위의 관리"에 대한 책임을 집중함
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request) {
        // 견적 및 참여자 엔티티 조회
        Quote quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.QUOTE_NOT_FOUND));

        User consumer = userRepository.findById(request.getConsumerId())
                .orElseThrow(() -> new CustomException(MemberErrorCode.USER_NOT_FOUND));

        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        // Quote 소유자 검증: Quote의 user와 consumerId가 일치해야 함
        if (!quote.getUser().getId().equals(consumer.getId())) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        
        // 중복 채팅방 여부 및 역할 검증
        validateExistingChatRoom(quote, consumer, seller);
        validateParticipantRoles(consumer, seller);

        // 채팅방 생성 및 저장
        ChatRoom chatRoom = ChatRoom.builder()
                .quote(quote)
                .consumer(consumer)
                .seller(seller)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomResponse.from(savedRoom);
    }

    @Transactional
    public ChatRoomResponse getChatRoom(UUID chatRoomId, UUID requesterId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자 검증
        validateParticipant(chatRoom, requesterId);
        return ChatRoomResponse.from(chatRoom);
    }

    @Transactional
    public List<ChatMessageResponse> getChatMessages(UUID chatRoomId, UUID requesterId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자만 메시지 조회 가능
        validateParticipant(chatRoom, requesterId);

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId).stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(UUID chatRoomId, UUID requesterId, ChatMessageReadRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 읽음 처리 요청자 검증
        validateParticipant(chatRoom, requesterId);

        // 동일 채팅방 내 지정 메시지 읽음 처리
        chatMessageRepository.findByChatRoomIdAndIdIn(chatRoomId, request.getMessageIds())
                .forEach(message -> message.markAsRead());
    }

    /**
     * 사용자가 참여한 채팅방 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> getChatRoomsByUser(UUID userId, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomRepository
            .findByConsumerIdOrSellerSellerIdOrderByCreatedAtDesc(userId, userId, pageable);
        
        return chatRooms.map(ChatRoomResponse::from);
    }

    private void validateExistingChatRoom(Quote quote, User consumer, Seller seller) {
        chatRoomRepository.findByQuoteIdAndConsumerIdAndSellerSellerId(
                quote.getId(), consumer.getId(), seller.getSellerId())
                .ifPresent(existingRoom -> {
                    throw new CustomException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
                });
    }

    private void validateParticipantRoles(User consumer, Seller seller) {
        if (!consumer.isConsumer()) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        if (!seller.canSell()) {
            throw new CustomException(MemberErrorCode.SELLER_NOT_APPROVED);
        }
    }

    // 채팅방 참여 여부를 검사해 비참여자의 접근을 차단
    private void validateParticipant(ChatRoom chatRoom, UUID requesterId) {
        boolean isParticipant = chatRoom.getConsumer().getId().equals(requesterId)
                || chatRoom.getSeller().getSellerId().equals(requesterId);
        if (!isParticipant) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}


