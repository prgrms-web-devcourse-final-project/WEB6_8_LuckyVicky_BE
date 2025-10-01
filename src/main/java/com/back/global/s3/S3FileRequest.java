package com.back.global.s3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 프론트 요청에 포함되는 파일 데이터 record
public record S3FileRequest(
        @NotBlank(message = "파일 URL은 필수입니다.")
        String url,

        @NotNull(message = "파일 타입은 필수입니다.")
        FileType type,

        @NotBlank(message = "파일 S3 Key는 필수입니다.")
        String s3Key,

        @NotBlank(message = "원본 파일명은 필수입니다.")
        String originalFileName
) {
}
