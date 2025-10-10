package com.back.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 결제 취소 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentCancelRequest {
    
    private String cancelReason; // 취소 사유
    private Integer cancelAmount; // 취소 금액 (null이면 전액 취소)
}
