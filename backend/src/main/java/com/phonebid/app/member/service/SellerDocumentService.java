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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private final TransactionTemplate transactionTemplate;
    private final PlatformTransactionManager transactionManager;

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "pdf", "JPG", "JPEG", "PNG", "PDF"
    );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 임시 파일 업로드 (회원가입 단계용, 인증 불필요)
     * S3에만 업로드하고 DB에는 저장하지 않음
     */
    public String uploadTempFile(SellerDocumentUploadRequestDto requestDto) {
        MultipartFile file = requestDto.getFile();
        DocumentType documentType = requestDto.getDocumentType();

        // 파일 검증
        validateFile(file);

        try {
            // 임시 파일명 생성 (UUID 기반)
            String tempFileName = generateTempFileName(documentType, file.getOriginalFilename());
            String uploadedFileUrl = s3Service.uploadFile(tempFileName, file);
            
            log.info("임시 파일 업로드 완료: documentType={}, fileName={}, url={}", 
                    documentType, file.getOriginalFilename(), uploadedFileUrl);
            
            return uploadedFileUrl;
        } catch (Exception e) {
            log.error("임시 파일 업로드 실패: documentType={}, fileName={}", 
                    documentType, file.getOriginalFilename(), e);
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 임시 파일명 생성
     */
    private String generateTempFileName(DocumentType documentType, String originalFilename) {
        String sanitizedFilename = sanitizeFilename(originalFilename);
        return String.format("temp/seller-documents/%s/%s-%s", 
                documentType.name().toLowerCase(), 
                UUID.randomUUID(), 
                sanitizedFilename);
    }

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

        String uploadedFileUrl = null;
        try {
            // Step 1: S3 업로드 (외부 I/O)
            String fileName = generateFileName(seller.getSellerId(), documentType, file.getOriginalFilename());
            uploadedFileUrl = s3Service.uploadFile(fileName, file);

            // Step 2: DB 저장 (트랜잭션) - 기존 레코드 교체 저장, 이전 파일 URL 반환
            UploadDbResult result = saveDocumentToDbReplacing(seller, documentType, uploadedFileUrl);

            // Step 3: 사후 처리 - 이전 S3 파일 삭제 (best-effort)
            if (result.previousFileUrl != null) {
                try {
                    s3Service.deleteFileByUrl(result.previousFileUrl);
                } catch (Exception e) {
                    log.warn("기존 S3 파일 삭제 실패(무시): {}", result.previousFileUrl, e);
                }
            }

            return SellerDocumentUploadResponseDto.from(result.savedDocument);

        } catch (Exception e) {
            // DB 저장 실패 등 예외 시 보상 트랜잭션: 업로드한 신규 파일을 S3에서 삭제
            if (uploadedFileUrl != null) {
                try {
                    s3Service.deleteFileByUrl(uploadedFileUrl);
                    log.warn("DB 저장 실패로 업로드 파일 롤백 완료: {}", uploadedFileUrl);
                } catch (Exception ignore) {
                    log.error("업로드 파일 롤백 실패 - 수동 삭제 필요: {}", uploadedFileUrl, ignore);
                }
            }
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 업로드 DB 처리 결과 모델
     */
    private static class UploadDbResult {
        private final SellerDocument savedDocument;
        private final String previousFileUrl;

        private UploadDbResult(SellerDocument savedDocument, String previousFileUrl) {
            this.savedDocument = savedDocument;
            this.previousFileUrl = previousFileUrl;
        }
    }

    /**
     * 기존 문서를 DB에서 교체 저장 (프로그램적 트랜잭션)
     * - 기존 레코드가 있으면 삭제하고 이전 파일 URL을 보관
     * - 새 레코드를 저장하고 이전 파일 URL을 반환하여 사후 처리에서 삭제
     */
    private UploadDbResult saveDocumentToDbReplacing(Seller seller, DocumentType documentType, String newFileUrl) {
        return transactionTemplate.execute(status -> {
            String previousFileUrl = null;

            // 기존 문서 조회 후 URL 보관 및 레코드 삭제
            Optional<SellerDocument> existingOpt = sellerDocumentRepository.findBySellerAndType(seller, documentType);
            if (existingOpt.isPresent()) {
                previousFileUrl = existingOpt.get().getFileUrl();
                sellerDocumentRepository.delete(existingOpt.get());
            }

            // 새 문서 저장
            SellerDocument sellerDocument = SellerDocument.builder()
                    .seller(seller)
                    .type(documentType)
                    .fileUrl(newFileUrl)
                    .build();

            SellerDocument saved = sellerDocumentRepository.save(sellerDocument);
            return new UploadDbResult(saved, previousFileUrl);
        });
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

    // previous (for reference)
    /*
    public SellerDocumentUploadResponseDto uploadDocument(String username, SellerDocumentUploadRequestDto requestDto) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        MultipartFile file = requestDto.getFile();
        DocumentType documentType = requestDto.getDocumentType();

        validateFile(file);

        try {
            String fileName = generateFileName(seller.getSellerId(), documentType, file.getOriginalFilename());
            String fileUrl = s3Service.uploadFile(fileName, file);

            sellerDocumentRepository.findBySellerAndType(seller, documentType)
                    .ifPresent(existingDocument -> {
                        try {
                            s3Service.deleteFileByUrl(existingDocument.getFileUrl());
                        } catch (Exception e) {
                            log.warn("기존 S3 파일 삭제 실패: {}", existingDocument.getFileUrl(), e);
                        }
                        sellerDocumentRepository.delete(existingDocument);
                    });

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
    */

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
    } */

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

        try {
            // Step 1: DB에서 삭제 (프로그램적 트랜잭션 보장)
            deleteDocumentFromDb(document);

            // Step 2: S3에서 파일 삭제 (외부 서비스)
            s3Service.deleteFileByUrl(document.getFileUrl());

        } catch (Exception e) {
            // S3 삭제 실패 시 보상 트랜잭션으로 DB 복구
            try {
                restoreDocumentToDb(document);
                log.warn("S3 삭제 실패로 인한 DB 복구 완료: fileUrl={}", document.getFileUrl());
            } catch (Exception restoreException) {
                log.error("DB 복구 실패 - 수동 처리 필요: fileUrl={}", document.getFileUrl(), restoreException);
                // TODO: 알림 서비스 또는 별도 처리 로직 필요
            }
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * DB에서 문서 삭제 (프로그램적 트랜잭션)
     */
    private void deleteDocumentFromDb(SellerDocument document) {
        transactionTemplate.executeWithoutResult(status -> sellerDocumentRepository.delete(document));
    }

    /**
     * DB에 문서 복구 (보상 트랜잭션, REQUIRES_NEW로 독립 보장)
     */
    private void restoreDocumentToDb(SellerDocument document) {
        TransactionTemplate requiresNewTemplate = new TransactionTemplate(transactionManager);
        requiresNewTemplate.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        requiresNewTemplate.executeWithoutResult(status -> sellerDocumentRepository.save(document));
    }
}
