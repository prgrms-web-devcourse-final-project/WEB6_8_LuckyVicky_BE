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

    @Version
    private Long version; // Optimistic Lock을 위한 버전 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;  // 일반 장바구니만 사용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id_ref", nullable = true)
    private com.back.domain.funding.entity.Funding funding;  // 펀딩 장바구니만 사용

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Boolean isSelected = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartType cartType = CartType.NORMAL;

    private String optionInfo; // 일반 상품의 경우에만 사용 (JSON 형태)
    
    // 펀딩 상품 관련 필드 (펀딩 상품일 때만 사용)
    private String fundingId; // 펀딩 고유 ID
    private Integer fundingPrice; // 펀딩 단일 가격 (상품 가격과 동일할 수 있음)
    private Integer fundingStock; // 펀딩 단일 재고

    public enum CartType {
        NORMAL,    // 일반 장바구니
        FUNDING;   // 펀딩 장바구니
        
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
    public Cart(User user, Product product, com.back.domain.funding.entity.Funding funding,
                Integer quantity, String optionInfo, CartType cartType, Boolean isSelected, 
                String fundingId, Integer fundingPrice, Integer fundingStock) {
        this.user = user;
        this.product = product;
        this.funding = funding;
        this.quantity = quantity != null && quantity > 0 ? quantity : 1;
        this.optionInfo = optionInfo;
        this.cartType = cartType != null ? cartType : CartType.NORMAL;
        this.isSelected = isSelected != null ? isSelected : true;
        this.fundingId = fundingId;
        this.fundingPrice = fundingPrice;
        this.fundingStock = fundingStock;
        
        // 검증: 타입에 맞는 참조가 있는지 확인
        if (this.cartType == CartType.NORMAL && this.product == null) {
            throw new IllegalArgumentException("일반 장바구니는 Product가 필수입니다.");
        }
        if (this.cartType == CartType.FUNDING && this.funding == null) {
            throw new IllegalArgumentException("펀딩 장바구니는 Funding이 필수입니다.");
        }
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

    // 펀딩 상품 관련 메서드
    public void updateFundingInfo(String fundingId, Integer fundingPrice, Integer fundingStock) {
        if (this.cartType != CartType.FUNDING) {
            throw new IllegalArgumentException("펀딩 장바구니가 아닙니다.");
        }
        this.fundingId = fundingId;
        this.fundingPrice = fundingPrice;
        this.fundingStock = fundingStock;
    }

    // 장바구니 타입 확인 메서드
    public boolean isNormalCart() {
        return this.cartType == CartType.NORMAL;
    }

    public boolean isFundingCart() {
        return this.cartType == CartType.FUNDING;
    }

    // 유효한 장바구니 아이템인지 확인 (null-safe)
    public boolean isValid() {
        // 1) 수량 검증 (null 또는 1 미만이면 무효)
        if (this.quantity == null || this.quantity <= 0) {
            return false;
        }

        // 2) 펀딩 장바구니 검증
        if (isFundingCart()) {
            if (this.funding == null) {
                return false;
            }
            Integer fundingStockFromEntity = this.funding.getStock();
            int availableStock = (this.fundingStock != null)
                    ? this.fundingStock
                    : (fundingStockFromEntity != null ? fundingStockFromEntity : 0);

            if (availableStock <= 0) {
                return false;
            }
            return this.quantity <= availableStock;
        }

        // 3) 일반 장바구니 검증
        if (this.product == null) {
            return false;
        }
        if (this.product.isDeleted()) {
            return false;
        }
        Integer productStock = this.product.getStock();
        if (productStock == null || productStock <= 0) {
            return false;
        }
        return this.quantity <= productStock;
    }

    // 총 가격 계산 (null-safe)
    public int getTotalPrice() {
        int qty = (this.quantity != null && this.quantity > 0) ? this.quantity : 0;
        int unitPrice;

        if (isFundingCart()) {
            if (this.fundingPrice != null) {
                unitPrice = this.fundingPrice;
            } else if (this.funding != null) {
                unitPrice = (int) this.funding.getPrice();
            } else {
                return 0;
            }
        } else {
            if (this.product == null) {
                return 0;
            }
            Integer discount = this.product.getDiscountPrice();
            Integer basePrice = this.product.getPrice();
            if (discount != null) {
                unitPrice = discount;
            } else if (basePrice != null) {
                unitPrice = basePrice;
            } else {
                return 0;
            }
        }

        return unitPrice * qty;
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
     * 타입에 따라 Product 또는 Funding 정보 반환
     */
    public ProductInfo getProductInfo() {
        if (isFundingCart() && this.funding != null) {
            // 펀딩 장바구니: Funding 정보 사용
            return new ProductInfo(
                this.funding.getId(),
                null, // 펀딩은 UUID 없음
                this.funding.getTitle(),
                this.fundingPrice != null ? this.fundingPrice : (int) this.funding.getPrice(),
                this.funding.getImageUrl()  // Funding 이미지
            );
        } else if (this.product != null) {
            // 일반 장바구니: Product 정보 사용
            String imageUrl = null;
            if (this.product.getImages() != null && !this.product.getImages().isEmpty()) {
                imageUrl = this.product.getImages().get(0).getFileUrl();
            }
            return new ProductInfo(
                this.product.getId(),
                this.product.getProductUuid(),
                this.product.getName(),
                this.product.getPrice(),
                imageUrl  // Product 첫 번째 이미지
            );
        }
        
        throw new IllegalStateException("유효하지 않은 장바구니 상태입니다.");
    }

    /**
     * 상품 정보를 담는 내부 클래스
     */
    public static class ProductInfo {
        private final Long id;
        private final java.util.UUID uuid;
        private final String name;
        private final Integer price;
        private final String imageUrl;

        public ProductInfo(Long id, java.util.UUID uuid, String name, Integer price, String imageUrl) {
            this.id = id;
            this.uuid = uuid;
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public Long getId() { return id; }
        public java.util.UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public Integer getPrice() { return price; }
        public String getImageUrl() { return imageUrl; }
    }
}