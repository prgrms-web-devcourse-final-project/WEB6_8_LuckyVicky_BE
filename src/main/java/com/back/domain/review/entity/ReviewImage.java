package com.back.domain.review.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.global.s3.FileType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 리뷰 이미지 엔티티
 * 리뷰에 첨부된 이미지를 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "review_images")
public class ReviewImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review; // 리뷰

    @Column(nullable = false)
    private String imageUrl; // 이미지 URL

    @Column(nullable = false)
    private String originalFileName; // 원본 파일명

    @Column(nullable = false)
    private String s3Key; // S3 Key

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType; // 파일 타입 (MAIN, ADDITIONAL, etc)

    @Column(nullable = false)
    private Integer sortOrder; // 이미지 순서

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false; // 논리 삭제 여부

    /**
     * 리뷰 설정
     */
    public void setReview(Review review) {
        this.review = review;
    }

    /**
     * 이미지 삭제
     */
    public void deleteImage() {
        this.isDeleted = true;
    }
}
