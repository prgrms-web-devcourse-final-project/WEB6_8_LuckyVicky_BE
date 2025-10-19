package com.back.domain.review.entity;

import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 엔티티
 * 상품에 대한 사용자 리뷰를 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product_id", columnList = "product_id"),
        @Index(name = "idx_review_user_id", columnList = "user_id"),
        @Index(name = "idx_review_created_date", columnList = "create_date")
})
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 리뷰 대상 상품

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 리뷰 작성자

    @Column(nullable = false)
    private Integer rating; // 평점 (1-5)

    @Column(columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    @Column(columnDefinition = "TEXT")
    private String hashtags; // 해시태그들 (JSON 형태로 저장: ["#해시태그1", "#해시태그2"])

    @Column
    private String productOption; // 상품 옵션 (예: "상품옵션1")

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0; // 좋아요 수

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPhotoReview = false; // 포토리뷰 여부

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false; // 논리 삭제 여부

    // 리뷰 이미지들
    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    // 리뷰 좋아요들
    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> likes = new ArrayList<>();

    /**
     * 리뷰 수정
     */
    public void updateReview(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    /**
     * 좋아요 수 증가
     */
    public void increaseLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 감소
     */
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview() {
        this.isDeleted = true;
    }

    /**
     * 이미지 추가
     */
    public void addImage(ReviewImage image) {
        this.images.add(image);
        image.setReview(this);
        this.isPhotoReview = !this.images.isEmpty();
    }

    /**
     * 이미지 제거
     * @param image 제거할 이미지
     */
    @SuppressWarnings("unused") // 리뷰 수정 시 이미지 삭제 기능에서 사용 예정
    public void removeImage(ReviewImage image) {
        this.images.remove(image);
        image.setReview(null);
        this.isPhotoReview = !this.images.isEmpty();
    }

    /**
     * 해시태그 목록을 JSON 문자열로 변환하여 저장
     */
    public void setHashtagsList(List<String> hashtagsList) {
        if (hashtagsList == null || hashtagsList.isEmpty()) {
            this.hashtags = null;
        } else {
            // 간단한 JSON 형태로 저장 (실제로는 ObjectMapper 사용 권장)
            this.hashtags = "[\"" + String.join("\",\"", hashtagsList) + "\"]";
        }
    }

    /**
     * JSON 문자열에서 해시태그 목록을 추출
     */
    public List<String> getHashtagsList() {
        if (hashtags == null || hashtags.trim().isEmpty()) {
            return List.of();
        }
        try {
            // 간단한 파싱 (실제로는 ObjectMapper 사용 권장)
            String cleanHashtags = hashtags.replaceAll("[\\[\\]\"]", "");
            if (cleanHashtags.isEmpty()) {
                return List.of();
            }
            return List.of(cleanHashtags.split(","));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 상품 옵션 설정
     */
    public void setProductOption(String productOption) {
        this.productOption = productOption;
    }
}
