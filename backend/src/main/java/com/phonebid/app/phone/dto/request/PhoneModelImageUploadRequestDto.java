package com.phonebid.app.phone.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
public class PhoneModelImageUploadRequestDto {
    @NotNull(message = "이미지 파일은 필수입니다.")
    @NotEmpty(message = "최소 1개 이상의 이미지 파일이 필요합니다.")
    private List<MultipartFile> files;
}

