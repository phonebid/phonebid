package com.phonebid.app.chat.service;

import com.phonebid.app.auction.domain.Bid;
import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.auction.repository.BidRepository;
import com.phonebid.app.auction.repository.QuoteRepository;
import com.phonebid.app.chat.domain.ChatMessage;
import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.domain.UserChatRoom;
import com.phonebid.app.chat.dto.request.ChatMessageReadRequest;
import com.phonebid.app.chat.dto.request.ChatRoomCreateRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatRoomResponse;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.repository.ChatMessageRepository;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.chat.repository.UserChatRoomRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.SellerRepository;
import com.phonebid.app.member.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final UserChatRoomRepository userChatRoomRepository;
    private final BidRepository bidRepository;
    private final SimpMessagingTemplate messagingTemplate;

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

        // UserChatRoom 생성: 구매자와 판매자 모두 채팅방에 참여
        UserChatRoom consumerUserChatRoom = UserChatRoom.builder()
                .user(consumer)
                .chatRoom(savedRoom)
                .build();
        
        UserChatRoom sellerUserChatRoom = UserChatRoom.builder()
                .user(seller.getUser())
                .chatRoom(savedRoom)
                .build();

        userChatRoomRepository.save(consumerUserChatRoom);
        userChatRoomRepository.save(sellerUserChatRoom);

        return ChatRoomResponse.from(savedRoom);
    }

    @Transactional
    public ChatRoomResponse getChatRoom(UUID chatRoomId, UUID requesterId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자 검증 (UserChatRoom 기반)
        validateParticipantByUserChatRoom(chatRoomId, requesterId);
        
        // 판매자 가게 이름
        String sellerName = chatRoom.getSeller().getStoreName();
        
        // 읽지 않은 메시지 수
        long unreadCount = chatMessageRepository.countUnreadMessagesByChatRoomIdAndUserId(
                chatRoomId, requesterId);
        
        return ChatRoomResponse.from(chatRoom, sellerName, null, null, unreadCount);
    }

    @Transactional
    public List<ChatMessageResponse> getChatMessages(UUID chatRoomId, UUID requesterId) {
        // 채팅방 존재 여부 확인
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자만 메시지 조회 가능 (UserChatRoom 기반)
        validateParticipantByUserChatRoom(chatRoomId, requesterId);

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId).stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(UUID chatRoomId, UUID requesterId, ChatMessageReadRequest request) {
        // 채팅방 존재 여부 확인
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 읽음 처리 요청자 검증 (UserChatRoom 기반)
        validateParticipantByUserChatRoom(chatRoomId, requesterId);

        // 동일 채팅방 내 지정 메시지 읽음 처리
        List<ChatMessage> readMessages = chatMessageRepository.findByChatRoomIdAndIdIn(chatRoomId, request.getMessageIds());
        readMessages.forEach(message -> message.markAsRead());
        
        // 읽음 처리된 메시지들을 DB에 저장 (명시적으로 저장하여 확실하게 반영)
        chatMessageRepository.saveAll(readMessages);

        // 읽음 처리된 메시지들을 WebSocket으로 브로드캐스트하여 상대방에게 알림
        // 읽음 상태 업데이트만을 위한 별도 토픽 사용 (무한 루프 방지)
        readMessages.forEach(message -> {
            ChatMessageResponse response = ChatMessageResponse.from(message);
            // 읽음 상태 업데이트 이벤트를 별도 토픽으로 전송
            messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/read", response);
        });
    }

    /**
     * 채팅방 나가기 (UserChatRoom의 deletedAt 설정)
     */
    @Transactional
    public void leaveChatRoom(UUID chatRoomId, UUID requesterId) {
        // 채팅방 존재 여부 확인
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // UserChatRoom 조회
        UserChatRoom userChatRoom = userChatRoomRepository
                .findActiveByUserIdAndChatRoomId(requesterId, chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

        // 이미 나간 채팅방인지 확인
        if (userChatRoom.isDeleted()) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ALREADY_LEFT);
        }

        // 삭제자 정보 조회 (사용자 ID를 문자열로 변환)
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.USER_NOT_FOUND));
        String deletedBy = requester.getId().toString();

        // 채팅방 나가기 (soft delete) - deletedAt, deletedBy, isDelete 모두 설정
        userChatRoom.leave(deletedBy);
        userChatRoomRepository.save(userChatRoom);
    }

    /**
     * 사용자가 참여한 채팅방 목록 조회 (페이징) - UserChatRoom 기반
     */
    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> getChatRoomsByUser(UUID userId, Pageable pageable) {
        Page<UserChatRoom> userChatRooms = userChatRoomRepository.findActiveChatRoomsByUserId(userId, pageable);
        
        return userChatRooms.map(userChatRoom -> {
            ChatRoom chatRoom = userChatRoom.getChatRoom();
            
            // 판매자 가게 이름
            String sellerName = chatRoom.getSeller().getStoreName();
            
            // 마지막 메시지
            Optional<ChatMessage> lastMessageOpt = chatMessageRepository
                    .findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());
            String lastMessage = lastMessageOpt
                    .map(ChatMessage::getContent)
                    .orElse(null);
            
            // 총 견적 가격 (해당 판매자의 입찰 가격)
            Integer totalPrice = null;
            Optional<Bid> bidOpt = bidRepository.findLatestByQuoteIdAndSellerId(
                    chatRoom.getQuote().getId(),
                    chatRoom.getSeller().getSellerId()
            );
            if (bidOpt.isPresent()) {
                Bid bid = bidOpt.get();
                totalPrice = bid.getTotalCost(); // 총 비용 (입찰가 + 요금제 + 추가지원금)
            }
            
            // 읽지 않은 메시지 수 (상대방이 보낸 메시지 중 읽지 않은 메시지)
            long unreadCount = chatMessageRepository.countUnreadMessagesByChatRoomIdAndUserId(chatRoom.getId(), userId);
            
            return ChatRoomResponse.from(chatRoom, sellerName, lastMessage, totalPrice, unreadCount);
        });
    }

    private void validateExistingChatRoom(Quote quote, User consumer, Seller seller) {
        chatRoomRepository.findByQuoteId(quote.getId())
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

    // 채팅방 참여 여부를 검사해 비참여자의 접근을 차단 (UserChatRoom 기반)
    private void validateParticipantByUserChatRoom(UUID chatRoomId, UUID requesterId) {
        boolean isActiveParticipant = userChatRoomRepository.existsActiveByUserIdAndChatRoomId(requesterId, chatRoomId);
        if (!isActiveParticipant) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    // 채팅방 참여 여부를 검사해 비참여자의 접근을 차단 (기존 방식 - 하위 호환성)
    private void validateParticipant(ChatRoom chatRoom, UUID requesterId) {
        boolean isParticipant = chatRoom.getConsumer().getId().equals(requesterId)
                || chatRoom.getSeller().getUser().getId().equals(requesterId);
        if (!isParticipant) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}


