package com.back.domain.review.repository;

import com.back.domain.product.product.entity.Product;
import com.back.domain.review.entity.Review;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 Repository
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 상품별 리뷰 목록 조회 (삭제되지 않은 리뷰만)
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createDate DESC")
    Page<Review> findByProductAndNotDeleted(@Param("product") Product product, Pageable pageable);

    /**
     * 상품별 리뷰 목록 조회 - N+1 방지를 위한 Fetch Join
     */
    @Query("SELECT DISTINCT r FROM Review r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH r.images ri " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createDate DESC")
    List<Review> findByProductWithUserAndImages(@Param("product") Product product, Pageable pageable);

    /**
     * 상품별 포토리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "AND r.isPhotoReview = true " +
           "ORDER BY r.createDate DESC")
    Page<Review> findPhotoReviewsByProduct(@Param("product") Product product, Pageable pageable);

    /**
     * 상품별 일반리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "AND r.isPhotoReview = false " +
           "ORDER BY r.createDate DESC")
    Page<Review> findGeneralReviewsByProduct(@Param("product") Product product, Pageable pageable);

    /**
     * 사용자별 리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.user = :user " +
           "AND r.isDeleted = false " +
           "ORDER BY r.createDate DESC")
    Page<Review> findByUserAndNotDeleted(@Param("user") User user, Pageable pageable);

    /**
     * 상품별 리뷰 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false")
    Long countByProductAndNotDeleted(@Param("product") Product product);

    /**
     * 상품별 포토리뷰 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "AND r.isPhotoReview = true")
    Long countPhotoReviewsByProduct(@Param("product") Product product);

    /**
     * 상품별 일반리뷰 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "AND r.isPhotoReview = false")
    Long countGeneralReviewsByProduct(@Param("product") Product product);

    /**
     * 상품별 평균 평점 조회
     */
    @Query("SELECT AVG(r.rating) FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false")
    Optional<Double> findAverageRatingByProduct(@Param("product") Product product);

    /**
     * 상품별 평점별 분포 조회
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "GROUP BY r.rating " +
           "ORDER BY r.rating")
    Object[][] findRatingDistributionByProduct(@Param("product") Product product);

    /**
     * 사용자가 특정 상품에 작성한 리뷰 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.user = :user " +
           "AND r.isDeleted = false")
    Optional<Review> findByProductAndUserAndNotDeleted(@Param("product") Product product, @Param("user") User user);

    /**
     * 좋아요 많은 순으로 리뷰 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "ORDER BY r.likeCount DESC, r.createDate DESC")
    Page<Review> findByProductOrderByLikeCountDesc(@Param("product") Product product, Pageable pageable);

    /**
     * 평점 높은 순으로 리뷰 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "ORDER BY r.rating DESC, r.createDate DESC")
    Page<Review> findByProductOrderByRatingDesc(@Param("product") Product product, Pageable pageable);

    /**
     * 평점 낮은 순으로 리뷰 조회
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "ORDER BY r.rating ASC, r.createDate DESC")
    Page<Review> findByProductOrderByRatingAsc(@Param("product") Product product, Pageable pageable);

    /**
     * 사용자별 좋아요 정보를 포함한 리뷰 목록 조회 - N+1 방지
     */
    @Query("SELECT DISTINCT r FROM Review r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH r.images ri " +
           "LEFT JOIN FETCH r.likes rl " +
           "WHERE r.product = :product " +
           "AND r.isDeleted = false " +
           "AND (:currentUserId IS NULL OR rl.user.id = :currentUserId OR rl.user.id IS NULL) " +
           "ORDER BY r.createDate DESC")
    List<Review> findByProductWithUserAndImagesAndLikes(
            @Param("product") Product product, 
            @Param("currentUserId") Long currentUserId, 
            Pageable pageable);

    /**
     * 리뷰 좋아요 수 증가 (동시성 안전)
     */
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void increaseLikeCount(@Param("reviewId") Long reviewId);

    /**
     * 리뷰 좋아요 수 감소 (동시성 안전)
     */
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = CASE WHEN r.likeCount > 0 THEN r.likeCount - 1 ELSE 0 END WHERE r.id = :reviewId")
    void decreaseLikeCount(@Param("reviewId") Long reviewId);
}
