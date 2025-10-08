package com.back.domain.payment.cash.dto.response;

import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캐시 환전 응답 DTO (작가 전용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashExchangeResponseDto {

    private Long transactionId; // 거래 ID
    private Long artistId; // 작가 ID
    private Integer amount; // 환전 금액
    private CashTransactionStatus status; // 거래 상태
    private String pgProvider; // PG사
    private String pgTransactionId; // PG 거래 ID
    private String pgApprovalNumber; // PG 승인번호
    private Integer balanceAfter; // 환전 후 잔액
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime completedAt; // 완료 시간

    public static CashExchangeResponseDto from(CashTransaction transaction) {
        return CashExchangeResponseDto.builder()
                .transactionId(transaction.getId())
                .artistId(transaction.getUser().getId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .pgProvider(transaction.getPgProvider())
                .pgTransactionId(transaction.getPgTransactionId())
                .pgApprovalNumber(transaction.getPgApprovalNumber())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreateDate())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}

