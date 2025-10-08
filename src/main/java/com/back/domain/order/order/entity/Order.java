package com.back.domain.order.order.entity;

import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.order.calculator.OrderAmountCalculator;
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

    public void validateOwnership(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new IllegalStateException("해당 주문에 대한 권한이 없습니다.");
        }
    }

    /**
     * 주문 생성 팩토리 메서드
     */
    public static Order createOrder(User user, List<OrderItem> orderItems, 
                                   String shippingAddress1, String shippingAddress2, String shippingZip,
                                   String recipientName, String recipientPhone, String deliveryRequest,
                                   PaymentMethod paymentMethod) {
        
        // 주문번호 생성
        String orderNumber = generateOrderNumber();
        
        // 금액 계산
        OrderAmountCalculator.OrderAmountInfo amountInfo = OrderAmountCalculator.calculateOrderAmount(orderItems);
        
        // 주문 생성
              Order order = Order.builder()
                      .user(user)
                      .orderNumber(orderNumber)
                      .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(amountInfo.totalQuantity())
                .totalAmount(amountInfo.totalAmount())
                .shippingFee(amountInfo.shippingFee())
                .finalAmount(amountInfo.finalAmount())
                .shippingAddress1(shippingAddress1)
                .shippingAddress2(shippingAddress2)
                .shippingZip(shippingZip)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .deliveryRequest(deliveryRequest)
                .paymentMethod(paymentMethod)
                .orderDate(LocalDateTime.now())
                .build();
        
        // 주문상품 연결
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        
        return order;
    }

    /**
     * 주문번호 생성
     */
    private static String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== Business Methods ====================

    /**
     * 환불 가능 여부 검증
     */
    public void validateRefundEligibility() {
        if (this.status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송완료된 주문만 환불 신청이 가능합니다.");
        }
        
        LocalDateTime refundDeadline = this.orderDate.plusDays(7);
        if (LocalDateTime.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("배송완료 후 7일 이내에만 환불 신청이 가능합니다.");
        }
    }

    /**
     * 교환 가능 여부 검증
     */
    public void validateExchangeEligibility() {
        if (this.status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송완료된 주문만 교환 신청이 가능합니다.");
        }
        
        LocalDateTime exchangeDeadline = this.orderDate.plusDays(7);
        if (LocalDateTime.now().isAfter(exchangeDeadline)) {
            throw new IllegalStateException("배송완료 후 7일 이내에만 교환 신청이 가능합니다.");
        }
    }

    /**
     * 환불 신청 처리
     */
    public void requestRefund() {
        validateRefundEligibility();
        this.status = OrderStatus.REFUND_REQUESTED;
    }

    /**
     * 교환 신청 처리
     */
    public void requestExchange() {
        validateExchangeEligibility();
        this.status = OrderStatus.EXCHANGE_REQUESTED;
    }

    /**
     * 환불 완료 처리
     */
    public void completeRefund() {
        this.status = OrderStatus.REFUND_COMPLETED;
    }

    /**
     * 교환 완료 처리
     */
    public void completeExchange() {
        this.status = OrderStatus.EXCHANGE_COMPLETED;
    }

    /**
     * 취소 처리
     */
    public void cancel() {
        this.status = OrderStatus.CANCELLATION_REQUESTED;
    }

    /**
     * 취소 완료 처리
     */
    public void completeCancellation() {
        this.status = OrderStatus.CANCELLATION_COMPLETED;
    }

    /**
     * 결제 가능 여부 확인
     * (주문 생성 시 바로 결제완료 상태이므로 항상 false)
     */
    public boolean canPay() {
        return false;
    }

    /**
     * 결제 완료 처리
     * (주문 생성 시 이미 결제완료 상태)
     */
    public void pay() {
        // 이미 결제 완료 상태
    }

}