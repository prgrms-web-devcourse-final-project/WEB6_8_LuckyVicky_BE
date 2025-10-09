package com.back.domain.payment.moriCash.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모리캐시 결제 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoriCashPaymentRequestDto {

    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId; // 주문 ID

    @NotNull(message = "결제 금액은 필수입니다")
    @Min(value = 100, message = "최소 결제 금액은 100원입니다")
    private Integer totalPrice; // 총 결제 금액

    @NotNull(message = "사용할 모리캐시 금액은 필수입니다")
    @Min(value = 0, message = "모리캐시 금액은 0원 이상이어야 합니다")
    private Integer usedMoriCash; // 사용할 모리캐시 금액
}

