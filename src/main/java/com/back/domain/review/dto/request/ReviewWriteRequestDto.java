package com.back.domain.review.dto.request;

import com.back.global.s3.S3FileRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 리뷰 작성 팝업용 요청 DTO
 * - images: 이미지 업로드 API로부터 받은 S3FileRequest 리스트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewWriteRequestDto {

    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId; // 상품 ID

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    private Integer rating; // 평점 (1-5)

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1000자 이하여야 합니다")
    private String content; // 리뷰 내용

    private List<S3FileRequest> images; // 리뷰 이미지 (S3 업로드 결과)

    @Size(max = 10, message = "해시태그는 최대 10개까지 입력 가능합니다")
    private List<String> hashtags; // 해시태그 목록

    @NotBlank(message = "상품 옵션은 필수입니다")
    private String productOption; // 선택한 상품 옵션 (예: "상품옵션1")

    @Builder.Default
    private Boolean isPhotoReview = false; // 포토리뷰 여부 (이미지가 있으면 자동으로 true)
    
    private String reviewType; // 리뷰 타입 ("PHOTO", "GENERAL")

    // 포토리뷰 여부 자동 설정 메서드
    public boolean isPhotoReview() {
        return (images != null && !images.isEmpty());
    }

    // 해시태그 유효성 검사
    public boolean hasValidHashtags() {
        if (hashtags == null) return true;
        return hashtags.stream().allMatch(tag -> 
            tag != null && !tag.trim().isEmpty() && tag.length() <= 20);
    }
}
