package com.back.domain.order.refund.entity;

import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refund_items")
public class RefundItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal refundPrice;

    @Builder
    public RefundItem(Refund refund, OrderItem orderItem, Integer quantity, BigDecimal refundPrice) {
        this.refund = refund;
        this.orderItem = orderItem;
        this.quantity = quantity;
        this.refundPrice = refundPrice;
    }

    // 환불 금액 계산
    public BigDecimal getTotalRefundAmount() {
        return this.refundPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
