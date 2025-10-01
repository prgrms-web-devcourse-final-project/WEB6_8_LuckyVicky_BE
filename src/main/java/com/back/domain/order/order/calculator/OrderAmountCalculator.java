package com.back.domain.order.order.calculator;

import com.back.domain.order.orderItem.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 금액 계산 유틸리티
 */
public class OrderAmountCalculator {
    
    public static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(3000);
    
    /**
     * 주문 금액 정보 계산
     */
    public static OrderAmountInfo calculateOrderAmount(List<OrderItem> orderItems) {
        return calculateOrderAmount(orderItems, DEFAULT_SHIPPING_FEE);
    }
    
    /**
     * 주문 금액 정보 계산 (배송비 지정)
     */
    public static OrderAmountInfo calculateOrderAmount(List<OrderItem> orderItems, BigDecimal shippingFee) {
        int totalQuantity = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal finalAmount = totalAmount.add(shippingFee);
        
        return new OrderAmountInfo(totalQuantity, totalAmount, shippingFee, finalAmount);
    }
    
    /**
     * 주문 금액 정보
     */
    public record OrderAmountInfo(
            Integer totalQuantity,
            BigDecimal totalAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount
    ) {}
}
