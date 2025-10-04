package com.back.domain.order.exchange.util;

import com.back.domain.order.exchange.dto.response.ExchangeResponseDto;
import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExchangeConverter {

    /**
     * Exchange 엔티티를 ResponseDto로 변환
     */
    public ExchangeResponseDto toResponseDto(Exchange exchange, List<ExchangeItem> exchangeItems) {
        List<ExchangeResponseDto.ExchangeItemResponseDto> itemDtos = exchangeItems.stream()
                .map(item -> new ExchangeResponseDto.ExchangeItemResponseDto(
                        item.getId(),
                        item.getOrderItem().getId(),
                        item.getOrderItem().getProduct().getName(),
                        item.getOrderItem().getProduct().getImages().isEmpty() ? null : 
                            item.getOrderItem().getProduct().getImages().get(0).getFileUrl(),
                        item.getQuantity(),
                        item.getOrderItem().getOptionInfo()
                ))
                .toList();

        return new ExchangeResponseDto(
                exchange.getId(),
                exchange.getOrder().getId(),
                exchange.getOrder().getOrderNumber(),
                exchange.getStatus(),
                exchange.getReason(),
                exchange.getDetailReason(),
                exchange.getExchangeMethod(),
                exchange.getAttachmentFiles() != null ? List.of(exchange.getAttachmentFiles().split(",")) : List.of(),
                exchange.getNewShippingAddress1(),
                exchange.getNewShippingAddress2(),
                exchange.getNewShippingZip(),
                exchange.getNewRecipientName(),
                exchange.getNewRecipientPhone(),
                exchange.getCreateDate(),
                exchange.getModifyDate(),
                itemDtos
        );
    }
}
