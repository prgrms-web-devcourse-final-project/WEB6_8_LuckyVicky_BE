package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import java.time.LocalDateTime;

/**
 * 캐시 관련 응답 DTO
 * 
 * 캐시 보유 현황 및 충전 내역 정보를 포함합니다.
 * 2025.09.22 생성.
 */
public class CashResponse {
    
    /**
     * 캐시 정보 응답
     */
    public record Balance(
            /** 현재 보유 캐시 */
            int currentBalance,
            /** 통화 */
            String currency,
            /** 업데이트 일시 */
            LocalDateTime updatedAt
    ) {}
    
    /**
     * 캐시 충전 내역 목록 응답
     */
    public static class HistoryList extends PageResponse<CashResponse.Transaction> {
        /** 캐시 현황 요약 정보 */
        private final SummaryDto summary;
        
        public HistoryList() {
            super();
            this.summary = null;
        }
        
        public HistoryList(SummaryDto summary, java.util.List<Transaction> content,
                          int page, int size, long totalElements, int totalPages,
                          boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
        
        public SummaryDto getSummary() {
            return summary;
        }
    }
    
    /**
     * 캐시 현황 요약 정보
     */
    public record SummaryDto(
            /** 현재 보유 캐시 */
            int currentBalance,
            /** 기간 내 충전 합계 (선택적) */
            int periodTotalRecharge,
            /** 기간 내 적립 포인트 합계 (선택적) */
            int periodTotalBonus
    ) {}
    
    /**
     * 캐시 거래 내역
     */
    public record Transaction(
            /** 거래 ID */
            String txId,
            /** 발생 일시 */
            LocalDateTime occurredAt,
            /** 카테고리 (UI 표시용) */
            String category,
            /** 충전 금액 */
            int chargeAmount,
            /** 사용 금액 */
            int useAmount,
            /** 거래 후 잔액 */
            int balanceAfter,
            /** 결제 수단 텍스트 */
            String paymentMethod,
            /** 상태 */
            String status,
            /** 링크 정보 (선택적) */
            Link links
    ) {}
    
    /**
     * 링크 정보
     */
    public record Link(
            /** 영수증/상세 링크 */
            String receipt
    ) {}
}
