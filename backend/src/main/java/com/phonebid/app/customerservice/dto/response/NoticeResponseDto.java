package com.phonebid.app.customerservice.dto.response;

import com.phonebid.app.customerservice.domain.Notice;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NoticeResponseDto {

    private UUID id;
    private String title;
    private Boolean isImportant;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoticeResponseDto from(Notice notice) {
        NoticeResponseDto dto = new NoticeResponseDto();
        dto.id = notice.getId();
        dto.title = notice.getTitle();
        dto.isImportant = notice.getIsImportant();
        dto.viewCount = notice.getViewCount();
        dto.createdAt = notice.getCreatedAt();
        dto.updatedAt = notice.getUpdatedAt();
        return dto;
    }
}

