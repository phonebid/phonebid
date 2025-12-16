package com.phonebid.app.customerservice.dto.response;

import com.phonebid.app.customerservice.domain.Inquiry;
import com.phonebid.app.customerservice.domain.InquiryCategory;
import com.phonebid.app.customerservice.domain.InquiryStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InquiryResponseDto {

    private UUID id;
    private InquiryCategory category;
    private String title;
    private InquiryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryResponseDto from(Inquiry inquiry) {
        InquiryResponseDto dto = new InquiryResponseDto();
        dto.id = inquiry.getId();
        dto.category = inquiry.getCategory();
        dto.title = inquiry.getTitle();
        dto.status = inquiry.getStatus();
        dto.createdAt = inquiry.getCreatedAt();
        dto.updatedAt = inquiry.getUpdatedAt();
        return dto;
    }
}

