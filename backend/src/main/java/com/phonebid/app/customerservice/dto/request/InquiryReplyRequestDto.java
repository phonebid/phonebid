package com.phonebid.app.customerservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryReplyRequestDto {

    @NotBlank(message = "답변 내용은 필수입니다.")
    private String content;
}

