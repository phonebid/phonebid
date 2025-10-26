package com.phonebid.app.chat.controller;

import com.phonebid.app.chat.dto.request.ChatMessageReadRequest;
import com.phonebid.app.chat.dto.request.ChatRoomCreateRequest;
import com.phonebid.app.chat.dto.response.ChatMessageResponse;
import com.phonebid.app.chat.dto.response.ChatRoomResponse;
import com.phonebid.app.common.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
@Validated
public class ChatRestController {

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "채팅방이 생성되었습니다.", response));
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(@PathVariable UUID chatRoomId) {
        ChatRoomResponse response = null;
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅방 정보를 조회했습니다.", response));
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(@PathVariable UUID chatRoomId) {
        List<ChatMessageResponse> responses = List.of();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅 메시지를 조회했습니다.", responses));
    }

    @PostMapping("/{chatRoomId}/messages/read")
    public ResponseEntity<ApiResponse<Void>> readMessages(@PathVariable UUID chatRoomId,
                                                          @RequestBody ChatMessageReadRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅 메시지를 읽음으로 처리했습니다.", null));
    }
}


