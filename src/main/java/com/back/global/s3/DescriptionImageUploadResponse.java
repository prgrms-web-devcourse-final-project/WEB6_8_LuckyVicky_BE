package com.back.global.s3;

// url만 반환하는 파일 업로드 응답 dto
public record DescriptionImageUploadResponse(
        String fileUrl
) {
}