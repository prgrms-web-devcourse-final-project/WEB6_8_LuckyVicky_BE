package com.back.domain.order.refund.dto.request;

import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.entity.RefundReasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * 환불 신청 요청 DTO
 */
public record RefundRequestDto(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,
        
        @NotNull(message = "환불할 주문상품 ID 목록은 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 주문상품을 선택해야 합니다.")
        List<Long> orderItemIds,
        
        @NotNull(message = "환불 사유 타입은 필수입니다.")
        RefundReasonType reasonType, // 환불 사유 타입 (재고 복원 여부 자동 판단)
        
        @NotBlank(message = "환불 사유는 필수입니다.")
        @Size(max = 100, message = "환불 사유는 100자를 초과할 수 없습니다.")
        String reason,
        
        @Size(max = 1000, message = "상세 사유는 1000자를 초과할 수 없습니다.")
        String detailReason,
        
        @NotNull(message = "환불 금액은 필수입니다.")
        BigDecimal refundAmount,
        
        @NotNull(message = "환불 방법은 필수입니다.")
        Refund.RefundMethod refundMethod,
        
        List<String> attachmentFiles // 첨부파일 목록
) {}
