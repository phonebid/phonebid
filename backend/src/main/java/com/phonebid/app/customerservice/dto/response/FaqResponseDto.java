package com.phonebid.app.customerservice.dto.response;

import com.phonebid.app.customerservice.domain.Faq;
import com.phonebid.app.customerservice.domain.FaqCategory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class FaqResponseDto {

    private UUID id;
    private FaqCategory category;
    private String question;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FaqResponseDto from(Faq faq) {
        FaqResponseDto dto = new FaqResponseDto();
        dto.id = faq.getId();
        dto.category = faq.getCategory();
        dto.question = faq.getQuestion();
        dto.viewCount = faq.getViewCount();
        dto.createdAt = faq.getCreatedAt();
        dto.updatedAt = faq.getUpdatedAt();
        return dto;
    }
}

