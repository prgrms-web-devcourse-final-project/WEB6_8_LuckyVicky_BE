package com.back.domain.order.refund.dto.response;

import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.entity.RefundItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 환불 응답 DTO
 */
public record RefundResponseDto(
        Long refundId,
        Long orderId,
        String orderNumber,
        Refund.RefundStatus status,
        String reason,
        String detailReason,
        BigDecimal refundAmount,
        Refund.RefundMethod refundMethod,
        List<String> attachmentFiles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<RefundItemResponseDto> refundItems
) {
    
    /**
     * 환불상품 응답 DTO
     */
    public record RefundItemResponseDto(
            Long refundItemId,
            Long orderItemId,
            String productName,
            String productThumbnailUrl,
            Integer quantity,
            BigDecimal refundPrice,
            BigDecimal totalRefundAmount,
            String optionInfo
    ) {}
}
