package com.back.domain.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토스페이먼츠 결제 승인 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentApproveResponse {
    
    private String paymentKey; // 결제 키
    private String orderId; // 주문 ID
    private String orderName; // 주문명
    private String status; // 결제 상태 (DONE, CANCELED, etc.)
    private Integer totalAmount; // 총 결제 금액
    private Integer balanceAmount; // 취소 가능 금액
    private String method; // 결제 수단 (카드, 계좌이체 등)
    private LocalDateTime requestedAt; // 결제 요청 시간
    private LocalDateTime approvedAt; // 결제 승인 시간
    
    // 카드 결제 정보
    private Card card;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private String company; // 카드사
        private String number; // 카드번호 (마스킹)
        private Integer installmentPlanMonths; // 할부 개월 수
        private String cardType; // 카드 타입 (신용/체크)
        private String ownerType; // 소유자 타입 (개인/법인)
        private String approveNo; // 승인 번호
    }
}

