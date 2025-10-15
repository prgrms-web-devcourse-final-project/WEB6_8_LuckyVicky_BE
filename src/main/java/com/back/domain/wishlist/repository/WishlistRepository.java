package com.back.domain.wishlist.repository;

import com.back.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 찜 등록 여부 조회
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    // 찜 삭제
    void deleteByUserIdAndProductId(Long userId, Long productId);
    // 상품별 찜 개수 조회
    Long countByProductId(Long productId);
    // 사용자별 찜 개수 조회
    long countByUserId(Long userId);

    // 사용자의 찜 목록 조회 (Product, User(작가) 정보 포함)

    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.product p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.images " +
            "WHERE w.user.id = :userId " +
            "AND p.isDeleted = false " +
            "ORDER BY w.createDate DESC")
    Page<Wishlist> findByUserIdWithProductAndArtist(@Param("userId") Long userId, Pageable pageable);

}
