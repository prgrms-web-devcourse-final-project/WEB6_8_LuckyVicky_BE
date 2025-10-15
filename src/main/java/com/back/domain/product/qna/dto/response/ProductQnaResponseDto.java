package com.back.domain.product.qna.dto.response;

import com.back.global.s3.UploadResultResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 상품 Q&A 상세 조회 응답 DTO
 */
@Schema(name = "ProductQnaResponseDto", description = "상품 Q&A 조회 응답 DTO")
public record ProductQnaResponseDto(
        @Schema(description = "상품 Q&A 글 번호", example = "1")
        Long id,
        @Schema(description = "Q&A 카테고리", example = "배송")
        String qnaCategory,
        @Schema(description = "Q&A 제목", example = "배송 문의합니다.")
        String qnaTitle,
        @Schema(description = "Q&A 내용", example = "상품 배송이 언제쯤 시작될까요?")
        String qnaDescription,
        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,
        @Schema(description = "작성일", example = "23.10.26")
        String createDate,
        @Schema(description = "첨부 이미지 파일 목록", example = "[" + "{\"url\":\"https://example.com/image1.jpg\",\"type\":\"ADDITIONAL\",\"s3Key\":\"product-images/uuid1.png\",\"originalFileName\":\"example.png\"}," + "{\"url\":\"https://example.com/image2.jpg\",\"fileType\":\"ADDITIONAL\",\"s3Key\":\"product-images/uuid2.png\",\"originalFileName\":\"example2.png\"}," + "]" )
        List<UploadResultResponse> qnaImages
) {
}
