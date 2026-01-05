package com.phonebid.app.phone.service;

import com.phonebid.app.phone.domain.PhoneModel;
import com.phonebid.app.phone.domain.PhoneModelImage;
import com.phonebid.app.phone.dto.response.PhoneModelImageResponseDto;
import com.phonebid.app.phone.dto.response.PhoneModelImageUploadResponseDto;
import com.phonebid.app.phone.repository.PhoneModelImageRepository;
import com.phonebid.app.phone.repository.PhoneModelRepository;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.errorcode.PhoneErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneModelImageService {

    private final PhoneModelImageRepository phoneModelImageRepository;
    private final PhoneModelRepository phoneModelRepository;
    private final S3Service s3Service;

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "JPG", "JPEG", "PNG", "GIF", "WEBP"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGES_PER_MODEL = 10;

    /**
     * 핸드폰 모델 이미지 업로드
     * 여러 이미지를 업로드할 수 있으며, 최대 10개까지 허용됩니다.
     * 관리자만 사용 가능합니다.
     */
    @Transactional
    public PhoneModelImageUploadResponseDto uploadPhoneModelImages(UUID phoneModelId, List<MultipartFile> files) {
        PhoneModel phoneModel = phoneModelRepository.findById(phoneModelId)
                .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));

        if (files == null || files.isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE);
        }

        List<PhoneModelImage> existingImages = phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(phoneModelId);
        int currentImageCount = existingImages.size();
        int newImageCount = files.size();

        if (currentImageCount + newImageCount > MAX_IMAGES_PER_MODEL) {
            throw new CustomException(PhoneErrorCode.PHONE_MODEL_IMAGE_LIMIT_EXCEEDED);
        }

        List<String> uploadedUrls = new ArrayList<>();
        List<PhoneModelImage> savedImages = new ArrayList<>();

        try {
            int startOrder = currentImageCount + 1;

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                validateImageFile(file);

                String fileName = generatePhoneModelImageFileName(phoneModelId, file.getOriginalFilename(), i);
                String imageUrl = s3Service.uploadFile(fileName, file);
                uploadedUrls.add(imageUrl);

                PhoneModelImage phoneModelImage = PhoneModelImage.builder()
                        .phoneModel(phoneModel)
                        .imageUrl(imageUrl)
                        .displayOrder(startOrder + i)
                        .build();

                PhoneModelImage saved = phoneModelImageRepository.save(phoneModelImage);
                savedImages.add(saved);
            }

            List<PhoneModelImageResponseDto> responseDtos = savedImages.stream()
                    .map(PhoneModelImageResponseDto::from)
                    .collect(Collectors.toList());

            return PhoneModelImageUploadResponseDto.from(responseDtos);

        } catch (IOException e) {
            log.error("핸드폰 모델 이미지 업로드 실패: {}", e.getMessage(), e);
            for (String url : uploadedUrls) {
                try {
                    s3Service.deleteFileByUrl(url);
                } catch (Exception ignore) {
                    log.error("업로드 실패 파일 롤백 실패: {}", url, ignore);
                }
            }
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 핸드폰 모델 이미지 삭제
     * 관리자만 사용 가능합니다.
     */
    @Transactional
    public void deletePhoneModelImage(UUID phoneModelId, UUID imageId) {
        phoneModelRepository.findById(phoneModelId)
                .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));

        PhoneModelImage phoneModelImage = phoneModelImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_IMAGE_NOT_FOUND));

        if (!phoneModelImage.getPhoneModel().getId().equals(phoneModelId)) {
            throw new CustomException(PhoneErrorCode.PHONE_MODEL_IMAGE_NOT_FOUND);
        }

        String imageUrl = phoneModelImage.getImageUrl();

        try {
            phoneModelImageRepository.delete(phoneModelImage);
            s3Service.deleteFileByUrl(imageUrl);
            log.info("핸드폰 모델 이미지 삭제 완료: phoneModelId={}, imageId={}", phoneModelId, imageId);
        } catch (Exception e) {
            log.error("핸드폰 모델 이미지 삭제 실패: {}", imageUrl, e);
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 핸드폰 모델의 모든 이미지 조회
     */
    @Transactional(readOnly = true)
    public List<PhoneModelImageResponseDto> getPhoneModelImages(UUID phoneModelId) {
        List<PhoneModelImage> images = phoneModelImageRepository.findByPhoneModelIdOrderByDisplayOrder(phoneModelId);
        return images.stream()
                .map(PhoneModelImageResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(MemberErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
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
     * 핸드폰 모델 이미지 파일명 생성
     */
    private String generatePhoneModelImageFileName(UUID phoneModelId, String originalFilename, int index) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sanitizedFilename = sanitizeFilename(originalFilename);
        return String.format("phone-models/%s/%s_%d_%s", phoneModelId, timestamp, index, sanitizedFilename);
    }

    /**
     * 파일명 보안 정리
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed.jpg";
        }
        return filename.trim()
                .replaceAll("[^a-zA-Z0-9가-힣._-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");
    }
}

