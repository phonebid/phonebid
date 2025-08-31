package com.phonebid.app.member.dto.request;

import com.phonebid.app.member.domain.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

/**
 * 판매자 문서 업로드 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDocumentUploadRequestDto {

    @NotNull(message = "문서 타입은 필수입니다.")
    private DocumentType documentType;

    @NotNull(message = "업로드할 파일은 필수입니다.")
    private MultipartFile file;
}
