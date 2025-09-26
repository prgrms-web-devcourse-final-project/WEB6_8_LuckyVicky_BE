package com.back.domain.cart.entity;

import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts")
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
        FUNDING;    // 펀딩 장바구니

        public static CartType fromString(String cartTypeStr) {
            if (cartTypeStr == null) {
                return NORMAL; // 기본값
            }
            try {
                return valueOf(cartTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 장바구니 타입입니다: " + cartTypeStr);
            }
        }
    }

    @Builder
    public Cart(User user, Product product, Integer quantity, String optionInfo,
                CartType cartType, Boolean isSelected) {
        this.user = user;
        this.product = product;
        this.quantity = quantity != null && quantity > 0 ? quantity : 1;
        this.optionInfo = optionInfo;
        this.cartType = cartType != null ? cartType : CartType.NORMAL;
        this.isSelected = isSelected != null ? isSelected : true;
    }

    // ===== 도메인 메서드 =====
    public void changeQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public void select() {
        this.isSelected = true;
    }

    public void unselect() {
        this.isSelected = false;
    }

    public void changeOptionInfo(String optionInfo) {
        this.optionInfo = optionInfo;
    }

    // ===== 도메인 메서드 (선택 상태 조회) =====
    public Boolean isSelected() {
        return this.isSelected;
    }




    /**
     * 장바구니 소유권 검증
     */
    public void validateOwnership(User user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        if (!isOwnedBy(user)) {
            throw new IllegalArgumentException("해당 장바구니 아이템에 대한 권한이 없습니다.");
        }
    }

    /**
     * 해당 사용자가 이 장바구니의 소유자인지 확인
     */
    public boolean isOwnedBy(User user) {
        if (user == null || this.user == null) {
            return false;
        }
        return this.user.getId().equals(user.getId());
    }

    /**
     * 상품 정보를 반환 (디미터의 법칙 적용)
     */
    public ProductInfo getProductInfo() {
        String imageUrl = this.product.getImages().stream()
                .filter(image -> "THUMBNAIL".equals(image.getFileType().name()))
                .findFirst()
                .map(image -> image.getFileUrl())
                .orElse(null);

        return new ProductInfo(
                this.product.getId(),
                this.product.getName(),
                this.product.getPrice(),
                imageUrl
        );
    }

    /**
     * 상품 정보를 담는 내부 클래스
     */
    public static class ProductInfo {
        private final Long id;
        private final String name;
        private final Integer price;
        private final String imageUrl;

        public ProductInfo(Long id, String name, Integer price, String imageUrl) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public Integer getPrice() { return price; }
        public String getImageUrl() { return imageUrl; }
    }
}