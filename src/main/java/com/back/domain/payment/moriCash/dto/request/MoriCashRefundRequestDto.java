package com.back.domain.payment.moriCash.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모리캐시 환불 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoriCashRefundRequestDto {

    @NotNull(message = "결제 ID는 필수입니다")
    private Long paymentId; // 결제 ID

    @NotNull(message = "환불 금액은 필수입니다")
    @Min(value = 100, message = "최소 환불 금액은 100원입니다")
    private Integer refundAmount; // 환불 금액

    @NotBlank(message = "환불 사유는 필수입니다")
    private String refundReason; // 환불 사유
}

