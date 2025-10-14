package com.back.domain.payment.settlement.dto.response;

import com.back.domain.payment.settlement.entity.Settlement;
import com.back.domain.payment.settlement.entity.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalResponseDto {

    private Long settlementId;
    private Long artistId;
    private Integer requestedAmount; // 매출금액
    private Integer commissionAmount; // 수수료
    private Integer netAmount; // 순수익
    private SettlementStatus status;
    private Integer remainingBalance; // 환전 후 남은 모리캐시
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static WithdrawalResponseDto from(Settlement settlement, Integer remainingBalance) {
        return WithdrawalResponseDto.builder()
                .settlementId(settlement.getId())
                .artistId(settlement.getArtist().getId())
                .requestedAmount(settlement.getRequestedAmount())
                .commissionAmount(settlement.getCommissionAmount())
                .netAmount(settlement.getNetAmount())
                .status(settlement.getStatus())
                .remainingBalance(remainingBalance)
                .createdAt(settlement.getCreateDate())
                .completedAt(settlement.getCompletedAt())
                .build();
    }
}
