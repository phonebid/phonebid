package com.phonebid.app.customerservice.dto.request;

import com.phonebid.app.customerservice.domain.InquiryCategory;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryUpdateRequestDto {

    private InquiryCategory category;

    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    private String content;
}

