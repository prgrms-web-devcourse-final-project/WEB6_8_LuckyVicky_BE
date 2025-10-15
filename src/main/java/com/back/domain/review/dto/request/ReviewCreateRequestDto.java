package com.back.domain.review.dto.request;

import com.back.global.s3.S3FileRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

/**
 * 리뷰 작성 요청 DTO
 * - images: 이미지 업로드 API로부터 받은 S3FileRequest 리스트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequestDto {

    @NotNull(message = "상품 UUID는 필수입니다")
    private UUID productUuid; // 상품 UUID

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    private Integer rating; // 평점 (1-5)

    @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다")
    private String content; // 리뷰 내용

    private List<S3FileRequest> images; // 리뷰 이미지 (S3 업로드 결과)

    private List<String> hashtags; // 해시태그 목록
    
    private String productOption; // 상품 옵션
}
