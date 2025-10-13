package com.back.domain.review.repository;

import com.back.domain.review.entity.Review;
import com.back.domain.review.entity.ReviewLike;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 리뷰 좋아요 Repository
 */
@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    /**
     * 사용자가 특정 리뷰에 좋아요를 눌렀는지 확인
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "WHERE rl.review = :review " +
           "AND rl.user = :user " +
           "AND rl.isDeleted = false")
    Optional<ReviewLike> findByReviewAndUserAndNotDeleted(@Param("review") Review review, @Param("user") User user);

    /**
     * 리뷰별 좋아요 개수 조회
     */
    @Query("SELECT COUNT(rl) FROM ReviewLike rl " +
           "WHERE rl.review = :review " +
           "AND rl.isDeleted = false")
    Long countByReviewAndNotDeleted(@Param("review") Review review);

    /**
     * 사용자가 좋아요를 누른 리뷰 목록 조회
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "WHERE rl.user = :user " +
           "AND rl.isDeleted = false")
    java.util.List<ReviewLike> findByUserAndNotDeleted(@Param("user") User user);
}
