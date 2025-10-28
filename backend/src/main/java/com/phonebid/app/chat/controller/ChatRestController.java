package com.phonebid.app.chat.controller;

import com.phonebid.app.chat.dto.request.ChatMessageReadRequest;
import com.phonebid.app.chat.dto.request.ChatRoomCreateRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatRoomResponse;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.service.ChatRoomService;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
@Validated
public class ChatRestController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "채팅방이 생성되었습니다.", response));
    }

    /**
     * 채팅방 상세 조회
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(@PathVariable UUID chatRoomId,
                                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID requesterId = resolveRequesterId(userDetails);
        ChatRoomResponse response = chatRoomService.getChatRoom(chatRoomId, requesterId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅방 정보를 조회했습니다.", response));
    }

    /**
     * 채팅 메시지 목록 조회
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(@PathVariable UUID chatRoomId,
                                                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID requesterId = resolveRequesterId(userDetails);
        List<ChatMessageResponse> responses = chatRoomService.getChatMessages(chatRoomId, requesterId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅 메시지를 조회했습니다.", responses));
    }

    /**
     * 채팅 메시지 읽음 처리
     */
    @PostMapping("/{chatRoomId}/messages/read")
    public ResponseEntity<ApiResponse<Void>> readMessages(@PathVariable UUID chatRoomId,
                                                          @Valid @RequestBody ChatMessageReadRequest request,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID requesterId = resolveRequesterId(userDetails);
        chatRoomService.markMessagesAsRead(chatRoomId, requesterId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅 메시지를 읽음으로 처리했습니다.", null));
    }

    // 요청자 ID 추출 (UserDetailsImpl 사용할 때 null 여부 확인)
    private UUID resolveRequesterId(UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        return userDetails.getUser().getId();
    }
}


