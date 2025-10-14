package com.back.domain.wishlist.repository;

import com.back.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 찜 등록 여부 조회
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // 찜 삭제
    void deleteByUserIdAndProductId(Long userId, Long productId);

    // 상품별 찜 개수 조회
    Long countByProductId(Long productId);

    /**
     * 대시보드용: 사용자의 찜한 상품 목록 조회 (페이징, Product와 User fetch join)
     * Product의 images는 별도로 BatchSize로 처리됨
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.product p " +
            "JOIN FETCH p.user " +
            "WHERE w.user.id = :userId " +
            "AND p.isDeleted = false " +
            "ORDER BY w.createDate DESC")
    Page<Wishlist> findWishlistsByUserIdForDashboard(@Param("userId") Long userId, Pageable pageable);

}
