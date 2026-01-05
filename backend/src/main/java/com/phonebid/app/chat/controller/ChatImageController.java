package com.phonebid.app.chat.controller;

import com.phonebid.app.chat.dto.response.ChatImageUploadResponseDto;
import com.phonebid.app.chat.service.ChatImageService;
import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatImageController {

    private final ChatImageService chatImageService;

    /**
     * 채팅 이미지 업로드
     * 채팅방에 이미지를 업로드하고 URL을 반환합니다.
     * 업로드된 URL은 WebSocket을 통해 메시지로 전송할 수 있습니다.
     */
    @PostMapping("/rooms/{chatRoomId}/images/upload")
    public ResponseEntity<ApiResponse<ChatImageUploadResponseDto>> uploadChatImage(
            @PathVariable UUID chatRoomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) {
        
        ChatImageUploadResponseDto responseDto = chatImageService.uploadChatImage(
                chatRoomId, userDetails.getUser(), file);
        
        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, "채팅 이미지 업로드가 성공적으로 완료되었습니다.", responseDto));
    }
}

