package com.back.domain.dashboard.artist.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 작가 지갑 관련 응답 DTO
 *
 * 작가의 지갑 잔액과 관련된 정보를 포함
 * 2025.09.22 생성
 */
public class ArtistCashResponse {

    /**
     * 지갑 잔액 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Balance {
        /** 현재 지갑 잔액 */
        private int currentBalance;
        /** 정산 대기 금액 (아직 잔액 미반영) */
        private int pendingSettlement;
        /** 환전 처리 중인 금액 */
        private int pendingWithdrawal;
        /** 지금 바로 환전 가능한 금액 (정책 반영) */
        private int withdrawable;
        /** 통화 */
        private String currency;
        /** 업데이트 시간 */
        private LocalDateTime updatedAt;
    }
}