package com.back.domain.wishlist.repository;

import com.back.domain.product.product.entity.Product;
import com.back.domain.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 찜 등록 여부 조회
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    // 찜 삭제
    void deleteByUserIdAndProductId(Long userId, Long productId);
    // 상품별 찜 개수 조회
    Long countByProductId(Long productId);

    // 특정 상품 해당하는 Wishlist(찜) 개수
    long countByProduct(Product product);

}
