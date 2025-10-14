package com.back.domain.wishlist.service;

import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.wishlist.entity.Wishlist;
import com.back.domain.wishlist.repository.WishlistRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /** 찜 등록 */
    @Transactional
    public UUID addWishlist(UUID productUuid, CustomUserDetails customUserDetails) {
        User user = validateAndGetUser(customUserDetails.getUser());
        Product product = validateAndGetProduct(productUuid);
        validateDuplicateWishlist(user.getId(), product.getProductUuid());

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        return wishlistRepository.save(wishlist).getProduct().getProductUuid();
    }

    /** 찜 삭제 */
    @Transactional
    public UUID removeWishlist(UUID productUuid, CustomUserDetails customUserDetails) {
        User user = validateAndGetUser(customUserDetails.getUser());
        Product product = validateAndGetProduct(productUuid);
        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), product.getProductUuid())) {
            throw new ServiceException("404", "위시리스트 항목을 찾을 수 없습니다.");
        }
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), product.getProductUuid());
        return product.getProductUuid();
    }

    /** 상품별 찜 개수 조회 */
    public Long getWishlistCount(UUID productUuid) {
        Product product = validateAndGetProduct(productUuid);
        return wishlistRepository.countByProductId(productUuid);
    }

    /** Validation 메서드 */
    // 사용자 검증
    private User validateAndGetUser(User user){
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));
    }
    // 상품 검증
    private Product validateAndGetProduct(UUID productUuid) {
        return productRepository.findByProductUuid(productUuid)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 상품입니다. UUID: " + productUuid));
    }
    // 중복 찜 검증
    private void validateDuplicateWishlist(Long userId, UUID productUuid) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productUuid)) {
            throw new ServiceException("409", "이미 위시리스트에 추가된 상품입니다.");
        }
    }
}
