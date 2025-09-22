package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;
import lombok.*;

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
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Balance {
        /** 현재 보유 캐시 */
        private int currentBalance;
        /** 통화 */
        private String currency;
        /** 업데이트 일시 */
        private LocalDateTime updatedAt;
    }
    
    /**
     * 캐시 충전 내역 목록 응답
     */
    @Getter
    @Setter
    public static class HistoryList extends PageResponse<CashResponse.Transaction> {
        /** 캐시 현황 요약 정보 */
        private SummaryDto summary;
        
        public HistoryList() {
            super();
        }
        
        public HistoryList(SummaryDto summary, java.util.List<Transaction> content,
                          int page, int size, long totalElements, int totalPages,
                          boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
    }
    
    /**
     * 캐시 현황 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        /** 현재 보유 캐시 */
        private int currentBalance;
        /** 기간 내 충전 합계 (선택적) */
        private int periodTotalRecharge;
        /** 기간 내 적립 포인트 합계 (선택적) */
        private int periodTotalBonus;
    }
    
    /**
     * 캐시 거래 내역
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
        /** 거래 ID */
        private String txId;
        /** 발생 일시 */
        private LocalDateTime occurredAt;
        /** 카테고리 (UI 표시용) */
        private String category;
        /** 충전 금액 */
        private int amount;
        /** 적립 포인트 */
        private int bonusPoint;
        /** 결제 수단 코드 */
        private String method;
        /** 결제 수단 텍스트 */
        private String methodText;
        /** 상태 */
        private String status;
        /** 링크 정보 (선택적) */
        private Link links;
    }
    
    /**
     * 링크 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        /** 영수증/상세 링크 */
        private String receipt;
    }
}
