package com.phonebid.app.member.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "seller_documents", indexes = {
    @Index(name = "idx_seller_documents_seller_id", columnList = "seller_id"),
    @Index(name = "idx_seller_documents_type", columnList = "type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DocumentType type;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Builder
    public SellerDocument(Seller seller, DocumentType type, String fileUrl) {
        validateDocumentCreation(seller, type, fileUrl);
        
        this.seller = seller;
        this.type = type;
        this.fileUrl = fileUrl;
    }

    // 비즈니스 메서드
    public boolean isBusinessLicense() {
        return type.isBusinessLicense();
    }

    public boolean isConsentForm() {
        return type.isConsentForm();
    }

    public String getFileName() {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return "";
        }
        
        // URL에서 파일명 추출 (마지막 / 이후 부분)
        String[] parts = fileUrl.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : fileUrl;
    }

    public boolean isS3Url() {
        return fileUrl != null && fileUrl.contains("amazonaws.com");
    }

    public String getDocumentSummary() {
        return String.format("%s - %s (%s)", 
            type.getDisplayName(), 
            getFileName(),
            getUpdatedAt() != null ? getUpdatedAt().toLocalDate().toString() : "N/A"
        );
    }

    public void updateFileUrl(String newFileUrl) {
        if (newFileUrl == null || newFileUrl.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE_URL);
        }
        
        this.fileUrl = newFileUrl.trim();
        // BaseEntity의 updatedAt은 JPA Auditing에 의해 자동으로 업데이트됨
    }

    // 검증 메서드
    private void validateDocumentCreation(Seller seller, DocumentType type, String fileUrl) {
        if (seller == null) {
            throw new CustomException(MemberErrorCode.MISSING_SELLER_INFO);
        }
        
        if (type == null) {
            throw new CustomException(MemberErrorCode.MISSING_DOCUMENT_TYPE);
        }
        
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE_URL);
        }
    }
} 