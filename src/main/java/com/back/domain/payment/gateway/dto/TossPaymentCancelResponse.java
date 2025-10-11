package com.back.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토스페이먼츠 결제 취소 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentCancelResponse {
    
    private String paymentKey; // 결제 키
    private String orderId; // 주문 ID
    private String status; // 결제 상태 (CANCELED)
    private Integer totalAmount; // 원래 결제 금액
    private Integer cancelAmount; // 취소된 금액
    private Integer balanceAmount; // 남은 금액
    private String cancelReason; // 취소 사유
    private LocalDateTime canceledAt; // 취소 시간
}

