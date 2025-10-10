package com.back.domain.payment.moriCash.dto.response;

import com.back.domain.payment.moriCash.entity.MoriCashPayment;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.payment.moriCash.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모리캐시 결제 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoriCashPaymentResponseDto {

    private Long paymentId; // 결제 ID
    private Long orderId; // 주문 ID
    private Long userId; // 사용자 ID
    private Integer totalPrice; // 총 결제 금액
    private Integer usedMoriCash; // 사용한 모리캐시 금액
    private MoriCashPaymentStatus status; // 결제 상태
    private String cashTransactionId; // 캐시 거래 ID
    private Integer balanceAfter; // 결제 후 잔액
    private TransactionType transactionType; // 거래 타입
    private String description; // 거래 설명
    private LocalDateTime paidAt; // 결제 완료 시간
    private LocalDateTime createdAt; // 생성 시간

    public static MoriCashPaymentResponseDto from(MoriCashPayment payment) {
        return MoriCashPaymentResponseDto.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .userId(payment.getUser().getId())
                .totalPrice(payment.getTotalPrice())
                .usedMoriCash(payment.getUsedMoriCash())
                .status(payment.getStatus())
                .cashTransactionId(payment.getCashTransactionId())
                .balanceAfter(payment.getBalanceAfter())
                .transactionType(payment.getTransactionType())
                .description(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreateDate())
                .build();
    }
}

