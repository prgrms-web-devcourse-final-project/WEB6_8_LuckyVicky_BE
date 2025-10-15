package com.back.domain.product.qna.dto.request;

import com.back.global.s3.S3FileRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 상품 Q&A 작성 요청을 받는 DTO
 */
public record ProductQnaRequestDto(
        @Schema(description = "Q&A 카테고리", example = "배송")
        @NotBlank(message = "Q&A 카테고리는 필수입니다.")
        String qnaCategory,

        @Schema(description = "Q&A 제목", example = "배송 문의합니다.")
        @NotBlank(message = "Q&A 제목은 필수입니다.")
        String qnaTitle,

        @Schema(description = "Q&A 내용", example = "상품 배송이 언제쯤 시작될까요?")
        @NotBlank(message = "Q&A 내용은 필수입니다.")
        String qnaDescription,

        @Schema(
                description = "첨부 이미지 파일 목록 (null 허용)",
                example = "["
                        + "{\"url\":\"https://example.com/image1.jpg\",\"type\":\"ADDITIONAL\",\"s3Key\":\"s3-key-1\",\"originalFileName\":\"image1.jpg\"},"
                        + "{\"url\":\"https://example.com/image2.jpg\",\"type\":\"ADDITIONAL\",\"s3Key\":\"s3-key-2\",\"originalFileName\":\"image2.jpg\"}"
                        + "]"
        )
        @Valid
        List<S3FileRequest> qnaImages
) {
}
