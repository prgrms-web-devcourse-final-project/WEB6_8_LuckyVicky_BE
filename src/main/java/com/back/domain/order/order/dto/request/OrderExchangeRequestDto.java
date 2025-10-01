package com.back.domain.order.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 교환 신청 요청 DTO
 */
public record OrderExchangeRequestDto(
        @NotBlank(message = "교환 사유는 필수입니다.")
        String exchangeReason,
        
        @NotEmpty(message = "교환할 상품을 선택해주세요.")
        List<Long> orderItemIds
) {}
