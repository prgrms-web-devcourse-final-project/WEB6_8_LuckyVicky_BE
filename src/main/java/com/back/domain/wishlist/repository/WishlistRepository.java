package com.back.domain.wishlist.repository;

import com.back.domain.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 찜 등록 여부 조회
    boolean existsByUserIdAndProductId(Long userId, UUID productUuid);
    // 찜 삭제
    void deleteByUserIdAndProductId(Long userId, UUID productUuid);
    // 상품별 찜 개수 조회
    Long countByProductId(UUID productUuid);

}
