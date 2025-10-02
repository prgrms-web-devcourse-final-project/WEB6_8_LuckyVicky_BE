package com.back.domain.order.exchange.dto.response;

import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 교환 응답 DTO
 */
public record ExchangeResponseDto(
        Long exchangeId,
        Long orderId,
        String orderNumber,
        Exchange.ExchangeStatus status,
        String reason,
        String detailReason,
        Exchange.ExchangeMethod exchangeMethod,
        List<String> attachmentFiles,
        String newShippingAddress1,
        String newShippingAddress2,
        String newShippingZip,
        String newRecipientName,
        String newRecipientPhone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ExchangeItemResponseDto> exchangeItems
) {
    
    /**
     * 교환상품 응답 DTO
     */
    public record ExchangeItemResponseDto(
            Long exchangeItemId,
            Long orderItemId,
            String productName,
            String productThumbnailUrl,
            Integer quantity,
            String optionInfo
    ) {}
}
