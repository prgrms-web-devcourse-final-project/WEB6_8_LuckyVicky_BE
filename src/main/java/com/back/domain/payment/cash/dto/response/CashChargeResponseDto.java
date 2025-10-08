package com.back.domain.payment.cash.dto.response;

import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import com.back.domain.payment.cash.entity.CashTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캐시 충전 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashChargeResponseDto {

    private Long transactionId; // 거래 ID
    private Long userId; // 사용자 ID
    private Integer amount; // 충전 금액
    private CashTransactionStatus status; // 거래 상태
    private String paymentMethod; // 결제 수단
    private String pgProvider; // PG사
    private String pgTransactionId; // PG 거래 ID
    private String pgApprovalNumber; // PG 승인번호
    private Integer balanceAfter; // 충전 후 잔액
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime completedAt; // 완료 시간

    public static CashChargeResponseDto from(CashTransaction transaction) {
        return CashChargeResponseDto.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUser().getId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPgProvider())
                .pgProvider(transaction.getPgProvider())
                .pgTransactionId(transaction.getPgTransactionId())
                .pgApprovalNumber(transaction.getPgApprovalNumber())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreateDate())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}

