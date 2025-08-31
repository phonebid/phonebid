package com.phonebid.app.member.dto.response;

import com.phonebid.app.member.domain.SellerDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자 문서 업로드 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDocumentUploadResponseDto {

    private UUID documentId;
    private String documentType;
    private String documentTypeDisplayName;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;

    /**
     * SellerDocument 엔티티로부터 응답 DTO 생성
     */
    public static SellerDocumentUploadResponseDto from(SellerDocument sellerDocument) {
        return SellerDocumentUploadResponseDto.builder()
                .documentId(sellerDocument.getId())
                .documentType(sellerDocument.getType().name())
                .documentTypeDisplayName(sellerDocument.getType().getDisplayName())
                .fileName(sellerDocument.getFileName())
                .fileUrl(sellerDocument.getFileUrl())
                .uploadedAt(sellerDocument.getUpdatedAt())
                .build();
    }
}
