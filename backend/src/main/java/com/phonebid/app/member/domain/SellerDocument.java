package com.phonebid.app.member.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Builder
    public SellerDocument(Seller seller, DocumentType type, String fileUrl) {
        validateDocumentCreation(seller, type, fileUrl);
        
        this.seller = seller;
        this.type = type;
        this.fileUrl = fileUrl;
        this.uploadedAt = LocalDateTime.now();
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
            uploadedAt.toLocalDate().toString()
        );
    }

    public void updateFileUrl(String newFileUrl) {
        if (newFileUrl == null || newFileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("파일 URL은 필수입니다.");
        }
        
        this.fileUrl = newFileUrl.trim();
        this.uploadedAt = LocalDateTime.now();
    }

    // 검증 메서드
    private void validateDocumentCreation(Seller seller, DocumentType type, String fileUrl) {
        if (seller == null) {
            throw new IllegalArgumentException("판매자 정보는 필수입니다.");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("문서 종류는 필수입니다.");
        }
        
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("파일 URL은 필수입니다.");
        }
    }
} 