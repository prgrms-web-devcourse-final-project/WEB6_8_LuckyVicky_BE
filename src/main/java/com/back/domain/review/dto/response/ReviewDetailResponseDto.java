package com.back.domain.review.dto.response;

import com.back.domain.review.entity.Review;
import com.back.domain.review.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 리뷰 상세 팝업용 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReviewDetailResponseDto {

    private Long reviewId; // 리뷰 ID
    private UUID productUuid; // 상품 UUID
    private String productName; // 상품명
    private String productOption; // 상품 옵션 (예: "상품옵션1")
    private Long userId; // 사용자 ID
    private String userName; // 사용자명
    private String userProfileImageUrl; // 사용자 프로필 이미지
    private Integer rating; // 평점
    private String content; // 리뷰 내용
    private Integer likeCount; // 좋아요 수
    private Boolean isPhotoReview; // 포토리뷰 여부
    private List<ReviewImageDetailDto> images; // 리뷰 이미지들 (상세보기용)
    private List<String> hashtags; // 해시태그들
    private Boolean isLiked; // 현재 사용자가 좋아요를 눌렀는지 여부
    private LocalDateTime createdAt; // 작성일
    private LocalDateTime modifiedAt; // 수정일

    /**
     * Review 엔티티를 ReviewDetailResponseDto로 변환
     */
    public static ReviewDetailResponseDto from(Review review, boolean isLiked) {
        return ReviewDetailResponseDto.builder()
                .reviewId(review.getId())
                .productUuid(review.getProduct().getProductUuid())
                .productName(review.getProduct().getName())
                .productOption(review.getProductOption() != null ? review.getProductOption() : "상품옵션1")
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .userProfileImageUrl(review.getUser().getProfileImageUrl())
                .rating(review.getRating())
                .content(review.getContent())
                .likeCount(review.getLikeCount())
                .isPhotoReview(review.getIsPhotoReview())
                .images(review.getImages().stream()
                        .map(ReviewImageDetailDto::from)
                        .collect(Collectors.toList()))
                .hashtags(review.getHashtagsList())
                .isLiked(isLiked)
                .createdAt(review.getCreateDate())
                .modifiedAt(review.getModifyDate())
                .build();
    }

    /**
     * Review 엔티티를 ReviewDetailResponseDto로 변환 (좋아요 정보 없음)
     */
    public static ReviewDetailResponseDto from(Review review) {
        return from(review, false);
    }

    /**
     * 리뷰 이미지 상세 DTO (팝업용)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewImageDetailDto {
        private Long imageId;
        private String imageUrl;
        private String thumbnailUrl; // 썸네일 URL
        private String originalFileName;
        private String s3Key;
        private String fileType;
        private Integer sortOrder;

        public static ReviewImageDetailDto from(ReviewImage image) {
            return ReviewImageDetailDto.builder()
                    .imageId(image.getId())
                    .imageUrl(image.getImageUrl())
                    .thumbnailUrl(image.getImageUrl()) // 현재는 원본 이미지 URL 사용 (필요시 썸네일 로직 추가)
                    .originalFileName(image.getOriginalFileName())
                    .s3Key(image.getS3Key())
                    .fileType(image.getFileType().name())
                    .sortOrder(image.getSortOrder())
                    .build();
        }
    }
}
