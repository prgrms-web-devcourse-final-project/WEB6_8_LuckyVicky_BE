package com.back.domain.order.order.entity;

import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false, length = 40)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal shippingFee;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    private String shippingAddress1;
    private String shippingAddress2;
    private String shippingZip;
    private String recipientName;
    private String recipientPhone;
    private String deliveryRequest;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();


    // 양방향 관계 헬퍼 메서드
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 도메인 메서드
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public void validateOwnership(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new IllegalStateException("해당 주문에 대한 권한이 없습니다.");
        }
    }
}