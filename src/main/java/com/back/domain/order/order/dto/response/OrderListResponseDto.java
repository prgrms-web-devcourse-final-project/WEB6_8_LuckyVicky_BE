package com.back.domain.order.order.dto.response;

import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 목록 응답 DTO
 */
public record OrderListResponseDto(
        Long orderId,
        String orderNumber,
        OrderStatus status,
        Integer totalQuantity,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal finalAmount,
        PaymentMethod paymentMethod,
        LocalDateTime orderDate,
        String representativeProductName,
        String representativeProductThumbnailUrl
) {}
