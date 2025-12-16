package com.phonebid.app.customerservice.dto.response;

import com.phonebid.app.customerservice.domain.Inquiry;
import com.phonebid.app.customerservice.domain.InquiryCategory;
import com.phonebid.app.customerservice.domain.InquiryReply;
import com.phonebid.app.customerservice.domain.InquiryStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InquiryDetailResponseDto {

    private UUID id;
    private InquiryCategory category;
    private String title;
    private String content;
    private InquiryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private InquiryReplyDto reply;

    @Getter
    public static class InquiryReplyDto {
        private UUID id;
        private String content;
        private String adminNickname;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static InquiryReplyDto from(InquiryReply reply) {
            if (reply == null) {
                return null;
            }
            InquiryReplyDto dto = new InquiryReplyDto();
            dto.id = reply.getId();
            dto.content = reply.getContent();
            dto.adminNickname = reply.getAdmin().getNickname();
            dto.createdAt = reply.getCreatedAt();
            dto.updatedAt = reply.getUpdatedAt();
            return dto;
        }
    }

    public static InquiryDetailResponseDto from(Inquiry inquiry, InquiryReply reply) {
        InquiryDetailResponseDto dto = new InquiryDetailResponseDto();
        dto.id = inquiry.getId();
        dto.category = inquiry.getCategory();
        dto.title = inquiry.getTitle();
        dto.content = inquiry.getContent();
        dto.status = inquiry.getStatus();
        dto.createdAt = inquiry.getCreatedAt();
        dto.updatedAt = inquiry.getUpdatedAt();
        dto.reply = InquiryReplyDto.from(reply);
        return dto;
    }
}

