package com.back.domain.dashboard.artist.dto.response;

import java.time.LocalDateTime;

/**
 * 작가 지갑 관련 응답 DTO
 *
 * 작가의 지갑 잔액과 관련된 정보를 포함
 * 2025.09.25 수정
 */
public class ArtistCashResponse {

    /**
     * 지갑 잔액 정보
     */
    public record Balance(
            /** 현재 지갑 잔액 */
            int currentBalance,
            /** 정산 대기 금액 (아직 잔액 미반영) */
            int pendingSettlement,
            /** 환전 처리 중인 금액 */
            int pendingWithdrawal,
            /** 지금 바로 환전 가능한 금액 (정책 반영) */
            int withdrawable,
            /** 통화 */
            String currency,
            /** 업데이트 시간 */
            LocalDateTime updatedAt
    ) {}
}
