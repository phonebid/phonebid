package com.phonebid.app.s3.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        Logger logger = LoggerFactory.getLogger(S3Service.class);
        
        if (sourceFileUrl == null || sourceFileUrl.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE_URL);
        }
        
        try {
            String sourceKey;
            
            // URL에서 key 추출 시도
            try {
                AmazonS3URI sourceS3Uri = new AmazonS3URI(sourceFileUrl);
                String sourceBucket = sourceS3Uri.getBucket();
                sourceKey = sourceS3Uri.getKey();
                
                if (sourceBucket == null || sourceKey == null || sourceKey.trim().isEmpty()) {
                    throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
                }
                
                if (!bucket.equals(sourceBucket)) {
                    throw new CustomException(MemberErrorCode.FILE_COPY_FAILED);
                }
            } catch (CustomException e) {
                throw e;
            } catch (IllegalArgumentException e) {
                // AmazonS3URI 파싱 실패 시 직접 URL 파싱
                // https://bucket.s3.region.amazonaws.com/key 형식 파싱
                String urlPattern = "https?://[^/]+/(.+)";
                if (sourceFileUrl.matches(urlPattern)) {
                    sourceKey = sourceFileUrl.replaceFirst("https?://[^/]+/", "");
                } else {
                    logger.error("URL 형식을 파싱할 수 없습니다: {}", sourceFileUrl);
                    throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
                }
            }
            
            // URL 인코딩된 key를 디코딩 (이미 디코딩된 경우를 대비해 try-catch)
            String decodedSourceKey;
            try {
                decodedSourceKey = URLDecoder.decode(sourceKey, StandardCharsets.UTF_8);
            } catch (Exception e) {
                // 이미 디코딩된 경우 원본 사용
                decodedSourceKey = sourceKey;
            }
            
            // 파일 복사
            s3Client.copyObject(bucket, decodedSourceKey, bucket, destinationFileName);
            
            // 복사된 파일의 URL 반환
            String resultUrl = s3Client.getUrl(bucket, destinationFileName).toString();
            
            return resultUrl;
            
        } catch (CustomException e) {
            logger.error("S3 파일 복사 실패 (CustomException): sourceUrl={}, destination={}", 
                    sourceFileUrl, destinationFileName, e);
            throw e;
        } catch (Exception e) {
            logger.error("S3 파일 복사 실패: sourceUrl={}, destination={}", 
                    sourceFileUrl, destinationFileName, e);
            throw new CustomException(MemberErrorCode.FILE_COPY_FAILED);
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

    /**
     * S3에서 특정 경로의 파일 목록 조회
     * @param prefix 조회할 경로 prefix (예: "temp/seller-documents/")
     * @return 파일 목록 (S3ObjectSummary 리스트)
     */
    public List<S3ObjectSummary> listFiles(String prefix) {
        List<S3ObjectSummary> files = new ArrayList<>();
        
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucket)
                    .withPrefix(prefix);
            
            ListObjectsV2Result result;
            do {
                result = s3Client.listObjectsV2(request);
                files.addAll(result.getObjectSummaries());
                request.setContinuationToken(result.getNextContinuationToken());
            } while (result.isTruncated());
            
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(S3Service.class);
            logger.error("S3 파일 목록 조회 실패: prefix={}", prefix, e);
            throw new CustomException(MemberErrorCode.FILE_LIST_FAILED);
        }
        
        return files;
    }

    /**
     * 오래된 임시 파일 삭제
     * @param prefix 조회할 경로 prefix (예: "temp/seller-documents/")
     * @param olderThanHours 삭제할 파일의 최소 경과 시간 (시간 단위)
     * @return 삭제된 파일 개수
     * @throws CustomException 파일 목록 조회 실패 시
     */
    public int deleteOldTempFiles(String prefix, long olderThanHours) {
        Logger logger = LoggerFactory.getLogger(S3Service.class);
        int deletedCount = 0;
        
        try {
            List<S3ObjectSummary> files = listFiles(prefix);
            Instant cutoffTime = Instant.now().minusSeconds(olderThanHours * 3600);
            
            for (S3ObjectSummary file : files) {
                Date lastModified = file.getLastModified();
                if (lastModified != null && lastModified.toInstant().isBefore(cutoffTime)) {
                    try {
                        s3Client.deleteObject(bucket, file.getKey());
                        deletedCount++;
                        logger.debug("오래된 임시 파일 삭제 완료: key={}, lastModified={}", 
                                file.getKey(), lastModified);
                    } catch (Exception e) {
                        logger.warn("임시 파일 삭제 실패: key={}", file.getKey(), e);
                    }
                }
            }
            
            logger.info("임시 파일 정리 완료: prefix={}, 삭제된 파일 수={}, 전체 파일 수={}", 
                    prefix, deletedCount, files.size());
            
        } catch (CustomException e) {
            logger.error("임시 파일 정리 중 오류 발생: prefix={}", prefix, e);
            throw e;
        } catch (Exception e) {
            logger.error("임시 파일 정리 중 오류 발생: prefix={}", prefix, e);
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }
        
        return deletedCount;
    }
}
