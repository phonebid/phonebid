package com.phonebid.app.s3.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;

import com.phonebid.app.s3.service.S3Service;
import java.io.IOException;

/**
 * S3 파일 업로드 컨트롤러 (테스트용)
 * 파일 업로드 요청을 처리하고 S3Service를 통해 S3에 파일을 업로드하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/s3-upload")
@RequiredArgsConstructor
public class UploadController {

    private final S3Service s3Service;

    /**
     * 파일을 S3의 test 폴더에 업로드
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     */
    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileName = "test/" + originalFileName; // test 폴더 아래에 저장
            String fileUrl = s3Service.uploadFile(fileName, file);
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * S3의 test 폴더에서 파일 삭제
     * @param fileName 삭제할 파일명
     * @return 삭제 성공 메시지
     */
    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName) {
        try {
            String fullFileName = "test/" + fileName; // test 폴더 아래의 파일 삭제
            s3Service.deleteFile(fullFileName);
            return ResponseEntity.ok("File deleted successfully: " + fullFileName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file: " + e.getMessage());
        }
    }
}
