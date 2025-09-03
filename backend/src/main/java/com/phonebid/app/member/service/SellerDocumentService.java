package com.phonebid.app.member.service;

import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.SellerDocument;
import com.phonebid.app.member.domain.DocumentType;
import com.phonebid.app.member.dto.request.SellerDocumentUploadRequestDto;
import com.phonebid.app.member.dto.response.SellerDocumentUploadResponseDto;
import com.phonebid.app.member.repository.SellerDocumentRepository;
import com.phonebid.app.member.repository.SellerRepository;
import com.phonebid.app.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 판매자 문서 서비스
 * 판매자 문서 업로드 및 관리 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SellerDocumentService {

    private final SellerDocumentRepository sellerDocumentRepository;
    private final SellerRepository sellerRepository;
    private final S3Service s3Service;

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "pdf", "JPG", "JPEG", "PNG", "PDF"
    );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 판매자 서류 업로드
     */
    public SellerDocumentUploadResponseDto uploadDocument(String username, SellerDocumentUploadRequestDto requestDto) {
        // 판매자 조회
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        MultipartFile file = requestDto.getFile();
        DocumentType documentType = requestDto.getDocumentType();

        // 파일 검증
        validateFile(file);

        try {
            // S3에 파일 업로드
            String fileName = generateFileName(seller.getSellerId(), documentType, file.getOriginalFilename());
            String fileUrl = s3Service.uploadFile(fileName, file);

            // 기존 문서가 있다면 삭제
            sellerDocumentRepository.findBySellerAndType(seller, documentType)
                    .ifPresent(existingDocument -> {
                        // S3에서 기존 파일 삭제
                        try {
                            s3Service.deleteFileByUrl(existingDocument.getFileUrl());
                        } catch (Exception e) {
                            log.warn("기존 S3 파일 삭제 실패: {}", existingDocument.getFileUrl(), e);
                        }
                        sellerDocumentRepository.delete(existingDocument);
                    });

            // 새로운 문서 엔티티 생성 및 저장
            SellerDocument sellerDocument = SellerDocument.builder()
                    .seller(seller)
                    .type(documentType)
                    .fileUrl(fileUrl)
                    .build();

            SellerDocument savedDocument = sellerDocumentRepository.save(sellerDocument);

            return SellerDocumentUploadResponseDto.from(savedDocument);

        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage(), e);
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE);
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(MemberErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * S3 파일명 생성
     * UUID 접두사 + 원본 파일명 조합
     */
    private String generateFileName(UUID sellerId, DocumentType documentType, String originalFilename) {
        // 파일명 보안을 위한 정리 (특수문자 제거, 공백 처리)
        String sanitizedFilename = sanitizeFilename(originalFilename);
        
        // seller-documents/{sellerId}/{documentType}/{UUID}-{originalFilename} 형식
        return String.format("seller-documents/%s/%s/%s-%s", 
                sellerId, 
                documentType.name().toLowerCase(), 
                UUID.randomUUID(), 
                sanitizedFilename);
    }

    /**
     * 파일명 보안 정리
     * 특수문자 제거 및 안전한 파일명으로 변환
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed";
        }
        
        // 위험한 문자들을 제거하고 공백을 언더스코어로 변경
        return filename.trim()
                .replaceAll("[^a-zA-Z0-9가-힣._-]", "_")  // 한글, 영문, 숫자, 점, 언더스코어, 하이픈만 허용
                .replaceAll("_{2,}", "_")  // 연속된 언더스코어를 하나로
                .replaceAll("^_+|_+$", "");  // 앞뒤 언더스코어 제거
    }

    /**
     * 판매자별 문서 목록 조회
     */
    public List<SellerDocumentUploadResponseDto> getSellerDocuments(String username) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        return sellerDocumentRepository.findBySellerOrderByCreatedAtDesc(seller)
                .stream()
                .map(SellerDocumentUploadResponseDto::from)
                .toList();
    }

    /**
     * 특정 문서 조회
     */
    public SellerDocumentUploadResponseDto getDocument(String username, DocumentType documentType) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        SellerDocument document = sellerDocumentRepository.findBySellerAndType(seller, documentType)
                .orElseThrow(() -> new CustomException(MemberErrorCode.DOCUMENT_NOT_FOUND));

        return SellerDocumentUploadResponseDto.from(document);
    }

    /**
     * 판매자 서류 삭제
     */
    public void deleteDocument(String username, DocumentType documentType) {
        // 판매자 조회
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        // 삭제할 문서 조회
        SellerDocument document = sellerDocumentRepository.findBySellerAndType(seller, documentType)
                .orElseThrow(() -> new CustomException(MemberErrorCode.DOCUMENT_NOT_FOUND));

        // S3에서 파일 삭제
        try {
            s3Service.deleteFileByUrl(document.getFileUrl());
            log.info("S3 파일 삭제 완료: {}", document.getFileUrl());
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", document.getFileUrl(), e);
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }

        // S3 삭제 성공 후 DB에서 문서 삭제
        sellerDocumentRepository.delete(document);
        
        log.info("판매자 문서 삭제 완료: sellerId={}, documentType={}, fileName={}", 
                seller.getSellerId(), documentType, document.getFileName());
    }

    /**
     * 판매자 서류 삭제 (파일 ID 기반)
     */
    public void deleteDocumentById(String username, UUID fileId) {
        // 판매자 조회
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        // 삭제할 문서 조회
        SellerDocument document = sellerDocumentRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.DOCUMENT_NOT_FOUND));

        // 해당 문서가 이 판매자의 것인지 확인
        if (!document.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new CustomException(MemberErrorCode.DOCUMENT_NOT_FOUND);
        }

        // S3에서 파일 삭제
        try {
            s3Service.deleteFileByUrl(document.getFileUrl());
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: fileUrl={}, fileName={}, error={}", 
                    document.getFileUrl(), document.getFileName(), e.getMessage(), e);
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }

        // S3 삭제 성공 후 DB에서 문서 삭제
        sellerDocumentRepository.delete(document);
    }
}
