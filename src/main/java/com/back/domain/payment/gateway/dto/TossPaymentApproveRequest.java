package com.back.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 결제 승인 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentApproveRequest {
    
    private String paymentKey; // 토스페이먼츠에서 발급한 결제 키
    private String orderId; // 주문 ID (우리가 생성한 고유 ID)
    private Integer amount; // 결제 금액
}

