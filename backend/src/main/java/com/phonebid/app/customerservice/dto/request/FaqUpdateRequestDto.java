package com.phonebid.app.customerservice.dto.request;

import com.phonebid.app.customerservice.domain.FaqCategory;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FaqUpdateRequestDto {

    private FaqCategory category;

    @Size(max = 200, message = "질문은 200자 이하여야 합니다.")
    private String question;

    private String answer;
}

