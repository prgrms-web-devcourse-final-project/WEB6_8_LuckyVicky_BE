package com.back.domain.cart.repository;

import com.back.domain.cart.entity.Cart;
import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 사용자와 상품, 장바구니 타입으로 조회
     */
    Optional<Cart> findByUserAndProductAndCartType(User user, Product product, Cart.CartType cartType);

    /**
     * 사용자의 모든 장바구니 삭제
     */
    int deleteByUser(User user);

    /**
     * 사용자의 특정 타입 장바구니 삭제
     */
    int deleteByUserAndCartType(User user, Cart.CartType cartType);

    /**
     * 사용자의 장바구니 조회 (상품 정보 포함, N+1 문제 해결)
     */
    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user")
    List<Cart> findByUserWithProduct(@Param("user") User user);

    /**
     * 선택된 장바구니 아이템 조회
     */
    List<Cart> findByUserAndIsSelectedTrue(User user);

    /**
     * 선택된 장바구니 아이템 조회 - N+1 방지를 위한 Fetch Join
     */
    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user AND c.isSelected = true")
    List<Cart> findByUserAndIsSelectedTrueWithProduct(@Param("user") User user);
    
    /**
     * 주문 완료 후 장바구니에서 제거 (UUID 기반)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user AND c.product.productUuid IN :productUuids")
    void deleteByUserAndProductUuidIn(@Param("user") User user, @Param("productUuids") List<java.util.UUID> productUuids);
}
