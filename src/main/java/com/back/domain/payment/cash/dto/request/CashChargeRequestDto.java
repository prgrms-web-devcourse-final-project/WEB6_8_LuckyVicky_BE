package com.back.domain.payment.cash.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐시 충전 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashChargeRequestDto {

    @NotNull(message = "충전 금액은 필수입니다")
    @Min(value = 1000, message = "최소 충전 금액은 1,000원입니다")
    private Integer amount; // 충전 금액

    @NotBlank(message = "결제 수단은 필수입니다")
    private String paymentMethod; // 결제 수단 (토스페이, 네이버페이 등)

    @NotBlank(message = "PG사는 필수입니다")
    private String pgProvider; // PG사 (TOSS)
}

