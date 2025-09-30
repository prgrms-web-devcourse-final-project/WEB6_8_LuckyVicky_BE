package com.back.domain.order.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 환불 신청 요청 DTO
 */
public record OrderRefundRequestDto(
        @NotBlank(message = "환불 사유는 필수입니다.")
        String refundReason,
        
        @NotEmpty(message = "환불할 상품을 선택해주세요.")
        List<Long> orderItemIds
) {}
