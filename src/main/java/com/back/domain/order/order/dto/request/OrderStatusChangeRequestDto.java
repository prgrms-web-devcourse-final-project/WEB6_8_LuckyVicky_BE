package com.back.domain.order.order.dto.request;

import com.back.domain.order.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 상태 변경 요청 DTO (관리자 권한 필요)
 */
public record OrderStatusChangeRequestDto(
        @NotNull(message = "변경할 주문 상태는 필수입니다.")
        OrderStatus status
) {}
