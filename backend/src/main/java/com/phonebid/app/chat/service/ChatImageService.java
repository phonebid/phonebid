package com.phonebid.app.chat.service;

import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.dto.response.ChatImageUploadResponseDto;
import com.phonebid.app.chat.errorcode.ChatErrorCode;
import com.phonebid.app.chat.repository.ChatRoomRepository;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatImageService {

    private final S3Service s3Service;
    private final ChatRoomRepository chatRoomRepository;

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "JPG", "JPEG", "PNG", "GIF", "WEBP"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 채팅 이미지 업로드
     * 채팅방에 이미지를 업로드하고 URL을 반환합니다.
     */
    @Transactional(readOnly = true)
    public ChatImageUploadResponseDto uploadChatImage(UUID chatRoomId, User user, MultipartFile file) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        validateParticipant(chatRoom, user.getId());
        validateImageFile(file);

        try {
            String fileName = generateChatImageFileName(chatRoomId, file.getOriginalFilename());
            String imageUrl = s3Service.uploadFile(fileName, file);
            return ChatImageUploadResponseDto.from(imageUrl);
        } catch (IOException e) {
            log.error("채팅 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 채팅방 참여자 검증
     */
    private void validateParticipant(ChatRoom chatRoom, UUID userId) {
        boolean isParticipant = chatRoom.getConsumer().getId().equals(userId)
                || chatRoom.getSeller().getUser().getId().equals(userId);

        if (!isParticipant) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(MemberErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 채팅 이미지 파일명 생성
     */
    private String generateChatImageFileName(UUID chatRoomId, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sanitizedFilename = sanitizeFilename(originalFilename);
        return String.format("chat/%s/%s_%s", chatRoomId, timestamp, sanitizedFilename);
    }

    /**
     * 파일명 보안 정리
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed.jpg";
        }
        return filename.trim()
                .replaceAll("[^a-zA-Z0-9가-힣._-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");
    }
}

