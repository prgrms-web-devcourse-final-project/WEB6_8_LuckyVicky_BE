package com.back.domain.payment.moriCash.dto.response;

import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모리캐시 잔액 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoriCashBalanceResponseDto {

    private Long balanceId; // 잔액 ID
    private Long userId; // 사용자 ID
    private Integer totalBalance; // 총 캐시 잔액
    private Integer availableBalance; // 사용 가능한 캐시 잔액
    private Integer frozenBalance; // 동결된 캐시 잔액
    private Integer totalCharged; // 총 충전 금액
    private Integer totalUsed; // 총 사용 금액

    public static MoriCashBalanceResponseDto from(MoriCashBalance balance) {
        return MoriCashBalanceResponseDto.builder()
                .balanceId(balance.getId())
                .userId(balance.getUser().getId())
                .totalBalance(balance.getTotalBalance())
                .availableBalance(balance.getAvailableBalance())
                .frozenBalance(balance.getFrozenBalance())
                .totalCharged(balance.getTotalCharged())
                .totalUsed(balance.getTotalUsed())
                .build();
    }
}

