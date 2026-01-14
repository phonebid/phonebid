package com.phonebid.app.s3.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * S3 파일 업로드 서비스
 * AWS S3를 통한 파일 업로드 및 관리 기능을 제공하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일을 S3에 업로드
     * @param fileName S3에 저장될 파일명
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public String uploadFile(String fileName, MultipartFile file) throws IOException {

        s3Client.putObject(bucket, fileName, file.getInputStream(), null);
        return s3Client.getUrl(bucket, fileName).toString();
        
    }

    /**
     * S3에서 파일 삭제
     * @param fileName 삭제할 파일명
     */
    public void deleteFile(String fileName) {
        s3Client.deleteObject(bucket, fileName);
    }

    /**
     * S3에서 파일 URL로 파일 삭제
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE_URL);
        }
        
        try {
            // AmazonS3URI를 사용하여 안전하게 URL 파싱
            AmazonS3URI s3Uri = new AmazonS3URI(fileUrl);
            String urlBucket = s3Uri.getBucket();
            String key = s3Uri.getKey();
            
            if (urlBucket == null || key == null || key.trim().isEmpty()) {
                throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
            }
            
            if (!bucket.equals(urlBucket)) {
                throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
            }
            
            // URL 인코딩된 key를 디코딩
            String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
            
            // 파일 삭제
            s3Client.deleteObject(bucket, decodedKey);
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * S3에서 파일 복사 (이동을 위한 복사)
     * @param sourceFileUrl 원본 파일 URL
     * @param destinationFileName 목적지 파일명
     * @return 복사된 파일의 URL
     */
    public String copyFile(String sourceFileUrl, String destinationFileName) {
        if (sourceFileUrl == null || sourceFileUrl.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE_URL);
        }
        
        try {
            // 원본 파일의 key 추출
            AmazonS3URI sourceS3Uri = new AmazonS3URI(sourceFileUrl);
            String sourceBucket = sourceS3Uri.getBucket();
            String sourceKey = sourceS3Uri.getKey();
            
            if (sourceBucket == null || sourceKey == null || sourceKey.trim().isEmpty()) {
                throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
            }
            
            if (!bucket.equals(sourceBucket)) {
                throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
            }
            
            // URL 인코딩된 key를 디코딩
            String decodedSourceKey = URLDecoder.decode(sourceKey, StandardCharsets.UTF_8);
            
            // 파일 복사
            s3Client.copyObject(bucket, decodedSourceKey, bucket, destinationFileName);
            
            // 복사된 파일의 URL 반환
            return s3Client.getUrl(bucket, destinationFileName).toString();
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * S3에서 파일 이동 (복사 후 삭제)
     * @param sourceFileUrl 원본 파일 URL
     * @param destinationFileName 목적지 파일명
     * @return 이동된 파일의 URL
     */
    public String moveFile(String sourceFileUrl, String destinationFileName) {
        // 파일 복사
        String newFileUrl = copyFile(sourceFileUrl, destinationFileName);
        
        // 원본 파일 삭제
        try {
            deleteFileByUrl(sourceFileUrl);
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(S3Service.class);
            logger.warn("파일 이동 후 원본 삭제 실패 (복사는 성공): sourceUrl={}, destination={}", 
                    sourceFileUrl, destinationFileName, e);
            // 복사는 성공했으므로 계속 진행
        }
        
        return newFileUrl;
    }
}
