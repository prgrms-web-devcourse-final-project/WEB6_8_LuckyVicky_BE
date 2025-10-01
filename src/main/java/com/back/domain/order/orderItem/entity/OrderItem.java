package com.back.domain.order.orderItem.entity;

import com.back.domain.order.order.entity.Order;
import com.back.domain.product.product.entity.Product;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(length = 255)
    private String optionInfo;


    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getTotalPrice() {
        return this.price.multiply(BigDecimal.valueOf(this.quantity));
    }

    /**
     * 주문상품 생성 팩토리 메서드
     */
    public static OrderItem createOrderItem(Product product, Integer quantity, String optionInfo) {
        validateOrderItem(product, quantity);
        
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .price(calculateOrderPrice(product))
                .optionInfo(processOptionInfo(optionInfo))
                .build();
    }
    
    /**
     * 주문상품 유효성 검증
     */
    private static void validateOrderItem(Product product, Integer quantity) {
        if (product == null) {
            throw new IllegalArgumentException("상품 정보가 필요합니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 재고: " + product.getStock() + ")");
        }
    }
    
    /**
     * 주문 가격 계산 (할인 적용)
     */
    private static BigDecimal calculateOrderPrice(Product product) {
        return BigDecimal.valueOf(product.getDiscountPrice()); // 할인된 가격 사용
    }
    
    /**
     * 옵션 정보 처리
     */
    private static String processOptionInfo(String optionInfo) {
        return optionInfo != null ? optionInfo.trim() : null;
    }
}