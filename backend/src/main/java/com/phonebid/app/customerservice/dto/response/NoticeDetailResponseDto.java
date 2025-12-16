package com.phonebid.app.customerservice.dto.response;

import com.phonebid.app.customerservice.domain.Notice;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NoticeDetailResponseDto {

    private UUID id;
    private String title;
    private String content;
    private Boolean isImportant;
    private Long viewCount;
    private String adminNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoticeDetailResponseDto from(Notice notice) {
        NoticeDetailResponseDto dto = new NoticeDetailResponseDto();
        dto.id = notice.getId();
        dto.title = notice.getTitle();
        dto.content = notice.getContent();
        dto.isImportant = notice.getIsImportant();
        dto.viewCount = notice.getViewCount();
        dto.adminNickname = notice.getAdmin() != null ? notice.getAdmin().getNickname() : null;
        dto.createdAt = notice.getCreatedAt();
        dto.updatedAt = notice.getUpdatedAt();
        return dto;
    }
}

