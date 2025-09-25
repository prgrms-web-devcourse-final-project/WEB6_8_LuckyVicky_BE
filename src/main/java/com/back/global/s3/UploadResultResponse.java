package com.back.global.s3;

// 파일 업로드 응답 record
public record UploadResultResponse(
        String url,
        String thumbnailUrl,
        FileType type,
        String s3Key,
        String originalFileName
) {
}
