package com.phonebid.app.member.controller;

import com.phonebid.app.common.dto.ApiResponse;
import com.phonebid.app.member.domain.DocumentType;
import com.phonebid.app.member.dto.request.SellerDocumentUploadRequestDto;
import com.phonebid.app.member.dto.response.SellerDocumentUploadResponseDto;
import com.phonebid.app.member.service.SellerDocumentService;
import com.phonebid.app.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 판매자 문서 컨트롤러
 * 판매자 문서 업로드 및 관리 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers/documents")
public class SellerDocumentController {

    private final SellerDocumentService sellerDocumentService;

    /**
     * 임시 파일 업로드 (회원가입 단계용, 인증 불필요)
     * POST /api/v1/sellers/documents/temp
     */
    @PostMapping(value = "/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadTempFile(
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file) {
        
        SellerDocumentUploadRequestDto requestDto = SellerDocumentUploadRequestDto.builder()
                .documentType(documentType)
                .file(file)
                .build();

        String fileUrl = sellerDocumentService.uploadTempFile(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, 
                        "임시 파일 업로드가 성공적으로 완료되었습니다.", fileUrl));
    }

    /**
     * 판매자 서류 업로드
     * POST /api/v1/sellers/documents
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SellerDocumentUploadResponseDto>> uploadDocument(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file) {
        
        //log.info("판매자 문서 업로드 요청: username={}, documentType={}, fileName={}", 
        //        userDetails.getUsername(), documentType, file.getOriginalFilename());

        SellerDocumentUploadRequestDto requestDto = SellerDocumentUploadRequestDto.builder()
                .documentType(documentType)
                .file(file)
                .build();

        SellerDocumentUploadResponseDto responseDto = sellerDocumentService.uploadDocument(
                userDetails.getUsername(), requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, 
                        "문서 업로드가 성공적으로 완료되었습니다.", responseDto));
    }

    /**
     * 판매자 문서 목록 조회
     * GET /api/v1/sellers/documents
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SellerDocumentUploadResponseDto>>> getSellerDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        log.info("판매자 문서 목록 조회 요청: username={}", userDetails.getUsername());

        List<SellerDocumentUploadResponseDto> documents = sellerDocumentService.getSellerDocuments(
                userDetails.getUsername());

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, 
                        "문서 목록 조회가 성공적으로 완료되었습니다.", documents));
    }

    /**
     * 특정 문서 조회
     * GET /api/v1/sellers/documents/{documentType}
     */
    @GetMapping("/{documentType}")
    public ResponseEntity<ApiResponse<SellerDocumentUploadResponseDto>> getDocument(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable DocumentType documentType) {
        
        log.info("판매자 특정 문서 조회 요청: username={}, documentType={}", 
                userDetails.getUsername(), documentType);

        SellerDocumentUploadResponseDto document = sellerDocumentService.getDocument(
                userDetails.getUsername(), documentType);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, 
                        "문서 조회가 성공적으로 완료되었습니다.", document));
    }

    /**
     * 판매자 서류 삭제 (파일 ID 기반)
     * DELETE /api/v1/sellers/documents/file/{fileId}
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID fileId) {
        
        sellerDocumentService.deleteDocumentById(userDetails.getUsername(), fileId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(HttpStatus.OK, 
                        "문서 삭제가 성공적으로 완료되었습니다.", null));
    }

    // /**
    //  * 판매자 서류 삭제 (문서 타입 기반)
    //  * DELETE /api/v1/sellers/documents/{documentType}
    //  */
    // @DeleteMapping("/{documentType}")
    // public ResponseEntity<ApiResponse<Void>> deleteDocument(
    //         @AuthenticationPrincipal UserDetailsImpl userDetails,
    //         @PathVariable DocumentType documentType) {
    //     
    //     sellerDocumentService.deleteDocument(userDetails.getUsername(), documentType);
    // 
    //     return ResponseEntity.ok()
    //             .body(ApiResponse.success(HttpStatus.OK, 
    //                     "문서 삭제가 성공적으로 완료되었습니다.", null));
    // }
    
}
