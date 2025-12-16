package com.phonebid.app.customerservice.dto.request;

import com.phonebid.app.customerservice.domain.FaqCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FaqCreateRequestDto {

    @NotNull(message = "FAQ 카테고리는 필수입니다.")
    private FaqCategory category;

    @NotBlank(message = "질문은 필수입니다.")
    @Size(max = 200, message = "질문은 200자 이하여야 합니다.")
    private String question;

    @NotBlank(message = "답변은 필수입니다.")
    private String answer;
}

