package com.phonebid.app.s3.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;

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
}
