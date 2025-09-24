package com.back.domain.cart.repository;

import com.back.domain.cart.entity.Cart;
import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUser(User user);

    List<Cart> findByUserAndCartType(User user, Cart.CartType cartType);

    Optional<Cart> findByUserAndProduct(User user, Product product);

    Optional<Cart> findByUserAndProductAndCartType(User user, Product product, Cart.CartType cartType);

    void deleteByUserAndProduct(User user, Product product);

    int deleteByUser(User user);

    int deleteByUserAndCartType(User user, Cart.CartType cartType);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user AND c.cartType = :cartType")
    List<Cart> findByUserAndCartTypeWithProduct(@Param("user") User user, @Param("cartType") Cart.CartType cartType);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user")
    List<Cart> findByUserWithProduct(@Param("user") User user);

    List<Cart> findByUserAndIsSelectedTrue(User user);
}
