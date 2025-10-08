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
 * 캐시 거래 내역 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashTransactionResponseDto {

    private Long transactionId; // 거래 ID
    private Long userId; // 사용자 ID
    private CashTransactionType transactionType; // 거래 타입 (CHARGING/EXCHANGE)
    private Integer amount; // 거래 금액
    private CashTransactionStatus status; // 거래 상태
    private String pgProvider; // PG사
    private String pgTransactionId; // PG 거래 ID
    private String pgApprovalNumber; // PG 승인번호
    private Integer balanceAfter; // 거래 후 잔액
    private String failureReason; // 실패 사유
    private String cancellationReason; // 취소 사유
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime completedAt; // 완료 시간
    private LocalDateTime cancelledAt; // 취소 시간

    public static CashTransactionResponseDto from(CashTransaction transaction) {
        return CashTransactionResponseDto.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUser().getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .pgProvider(transaction.getPgProvider())
                .pgTransactionId(transaction.getPgTransactionId())
                .pgApprovalNumber(transaction.getPgApprovalNumber())
                .balanceAfter(transaction.getBalanceAfter())
                .failureReason(transaction.getFailureReason())
                .cancellationReason(transaction.getCancellationReason())
                .createdAt(transaction.getCreateDate())
                .completedAt(transaction.getCompletedAt())
                .cancelledAt(transaction.getCancelledAt())
                .build();
    }
}

