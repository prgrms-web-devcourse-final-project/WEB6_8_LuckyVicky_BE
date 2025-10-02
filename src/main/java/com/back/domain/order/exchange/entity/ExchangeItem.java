package com.back.domain.order.exchange.entity;

import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exchange_items")
public class ExchangeItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public ExchangeItem(Exchange exchange, OrderItem orderItem, Integer quantity) {
        this.exchange = exchange;
        this.orderItem = orderItem;
        this.quantity = quantity;
    }
}
