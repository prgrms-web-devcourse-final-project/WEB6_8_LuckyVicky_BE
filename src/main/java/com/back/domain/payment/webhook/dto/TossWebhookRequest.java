package com.back.domain.payment.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 Webhook 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossWebhookRequest {

    private String paymentKey;      // 결제 키
    private String orderId;         // 주문 ID (우리가 생성한 ID)
    private Integer amount;         // 결제 금액
    private String status;          // 결제 상태 (DONE, CANCELED 등)
    private String failureCode;     // 실패 코드
    private String failureMessage;  // 실패 메시지
}

