package com.back.domain.review.dto.response;

import com.back.domain.review.entity.Review;
import com.back.domain.review.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리뷰 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReviewResponseDto {

    private Long reviewId; // 리뷰 ID
    private Long productId; // 상품 ID
    private String productName; // 상품명
    private Long userId; // 사용자 ID
    private String userName; // 사용자명
    private String userProfileImageUrl; // 사용자 프로필 이미지
    private Integer rating; // 평점
    private String content; // 리뷰 내용
    private Integer likeCount; // 좋아요 수
    private Boolean isPhotoReview; // 포토리뷰 여부
    private List<ReviewImageResponseDto> images; // 리뷰 이미지들
    private List<String> hashtags; // 해시태그들
    private String productOption; // 상품 옵션
    private Boolean isLiked; // 현재 사용자가 좋아요를 눌렀는지 여부
    private LocalDateTime createdAt; // 작성일
    private LocalDateTime modifiedAt; // 수정일

    /**
     * Review 엔티티를 ReviewResponseDto로 변환
     */
    public static ReviewResponseDto from(Review review, boolean isLiked) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .userProfileImageUrl(review.getUser().getProfileImageUrl())
                .rating(review.getRating())
                .content(review.getContent())
                .likeCount(review.getLikeCount())
                .isPhotoReview(review.getIsPhotoReview())
                .images(review.getImages().stream()
                        .map(ReviewImageResponseDto::from)
                        .collect(Collectors.toList()))
                .hashtags(review.getHashtagsList())
                .productOption(review.getProductOption())
                .isLiked(isLiked)
                .createdAt(review.getCreateDate())
                .modifiedAt(review.getModifyDate())
                .build();
    }

    /**
     * 리뷰 이미지 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewImageResponseDto {
        private Long imageId;
        private String imageUrl;
        private String originalFileName;
        private String s3Key;
        private String fileType;
        private Integer sortOrder;

        public static ReviewImageResponseDto from(ReviewImage image) {
            return ReviewImageResponseDto.builder()
                    .imageId(image.getId())
                    .imageUrl(image.getImageUrl())
                    .originalFileName(image.getOriginalFileName())
                    .s3Key(image.getS3Key())
                    .fileType(image.getFileType().name())
                    .sortOrder(image.getSortOrder())
                    .build();
        }
    }
}
