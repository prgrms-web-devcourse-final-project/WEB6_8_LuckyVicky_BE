package com.back.domain.cart.entity;

import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart")
public class Cart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Boolean isSelected = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartType cartType = CartType.NORMAL;

    private String optionInfo;

    public enum CartType {
        NORMAL,    // 일반 장바구니
        FUNDING    // 펀딩 장바구니
    }

    @Builder
    public Cart(User user, Product product, Integer quantity, String optionInfo,
               CartType cartType, Boolean isSelected) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.optionInfo = optionInfo;
        this.cartType = cartType;
        this.isSelected = isSelected;
    }

    // public setter 메서드들 추가
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setOptionInfo(String optionInfo) {
        this.optionInfo = optionInfo;
    }
}
