package com.phonebid.app.customerservice.dto.response;

import com.phonebid.app.customerservice.domain.Faq;
import com.phonebid.app.customerservice.domain.FaqCategory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class FaqDetailResponseDto {

    private UUID id;
    private FaqCategory category;
    private String question;
    private String answer;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FaqDetailResponseDto from(Faq faq) {
        FaqDetailResponseDto dto = new FaqDetailResponseDto();
        dto.id = faq.getId();
        dto.category = faq.getCategory();
        dto.question = faq.getQuestion();
        dto.answer = faq.getAnswer();
        dto.viewCount = faq.getViewCount();
        dto.createdAt = faq.getCreatedAt();
        dto.updatedAt = faq.getUpdatedAt();
        return dto;
    }
}

