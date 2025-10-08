package com.back.domain.payment.moriCash.dto.response;

import com.back.domain.payment.moriCash.entity.MoriCashPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모리캐시 환불 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoriCashRefundResponseDto {

    private Long paymentId; // 결제 ID
    private Long orderId; // 주문 ID
    private Long userId; // 사용자 ID
    private String refundId; // 환불 ID
    private Integer refundPrice; // 환불 금액
    private Integer balanceAfter; // 환불 후 잔액
    private LocalDateTime refundedAt; // 환불 완료 시간

    public static MoriCashRefundResponseDto from(MoriCashPayment payment, Integer balanceAfter) {
        return MoriCashRefundResponseDto.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .userId(payment.getUser().getId())
                .refundId(payment.getRefundId())
                .refundPrice(payment.getRefundPrice())
                .balanceAfter(balanceAfter)
                .refundedAt(LocalDateTime.now())
                .build();
    }
}

