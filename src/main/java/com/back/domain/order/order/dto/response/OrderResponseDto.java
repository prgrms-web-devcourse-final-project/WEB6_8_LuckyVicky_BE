package com.back.domain.order.order.dto.response;

import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 주문 상세 응답 DTO
 */
public record OrderResponseDto(
        Long orderId,
        String orderNumber,
        OrderStatus status,
        Integer totalQuantity,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal finalAmount,
        String shippingAddress1,
        String shippingAddress2,
        String shippingZip,
        String recipientName,
        String recipientPhone,
        String deliveryRequest,
        PaymentMethod paymentMethod,
        LocalDateTime orderDate,
        List<OrderItemResponseDto> orderItems
) {
    
    /**
     * 주문 상품 응답 DTO
     */
    public record OrderItemResponseDto(
            Long orderItemId,
            UUID productUuid,
            String productName,
            String productThumbnailUrl,
            Integer quantity,
            BigDecimal price,
            BigDecimal totalPrice,
            String optionInfo
    ) {}
}
