package com.back.domain.order.refund.util;

import com.back.domain.order.refund.dto.response.RefundResponseDto;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.entity.RefundItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class RefundConverter {

    /**
     * Refund 엔티티를 ResponseDto로 변환
     */
    public RefundResponseDto toResponseDto(Refund refund, List<RefundItem> refundItems) {
        List<RefundResponseDto.RefundItemResponseDto> itemDtos = refundItems.stream()
                .map(item -> new RefundResponseDto.RefundItemResponseDto(
                        item.getId(),
                        item.getOrderItem().getId(),
                        item.getOrderItem().getProduct().getName(),
                        item.getOrderItem().getProduct().getImages().isEmpty() ? null : 
                            item.getOrderItem().getProduct().getImages().get(0).getFileUrl(),
                        item.getQuantity(),
                        item.getRefundPrice(),
                        item.getRefundPrice().multiply(BigDecimal.valueOf(item.getQuantity())), // totalRefundAmount 계산
                        item.getOrderItem().getOptionInfo()
                ))
                .toList();

        return new RefundResponseDto(
                refund.getId(),
                refund.getOrder().getId(),
                refund.getOrder().getOrderNumber(),
                refund.getStatus(),
                refund.getReason(),
                refund.getDetailReason(),
                refund.getRefundAmount(),
                refund.getRefundMethod(),
                refund.getAttachmentFiles() != null ? List.of(refund.getAttachmentFiles().split(",")) : List.of(),
                refund.getCreateDate(),
                refund.getModifyDate(),
                itemDtos
        );
    }
}
