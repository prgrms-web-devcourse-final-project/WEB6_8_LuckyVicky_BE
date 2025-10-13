package com.back.domain.review.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 리뷰 좋아요 엔티티
 * 사용자가 리뷰에 좋아요를 누른 정보를 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "review_likes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_id"}),
       indexes = {
           @Index(name = "idx_review_like_review_id", columnList = "review_id"),
           @Index(name = "idx_review_like_user_id", columnList = "user_id")
       })
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review; // 리뷰

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 좋아요를 누른 사용자

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false; // 논리 삭제 여부

    /**
     * 좋아요 취소
     */
    public void cancelLike() {
        this.isDeleted = true;
    }

    /**
     * 좋아요 복원
     */
    public void restoreLike() {
        this.isDeleted = false;
    }
}
