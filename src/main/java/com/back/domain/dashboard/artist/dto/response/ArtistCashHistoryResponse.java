package com.back.domain.dashboard.artist.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작가 캐시 입금/환전 내역 응답 DTO
 * 
 * 작가의 캐시 거래 내역 정보를 포함
 * 2025.09.24 생성
 */
public class ArtistCashHistoryResponse {

    /**
     * 캐시 내역 목록 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class List {
        /** 요약 정보 */
        private Summary summary;
        /** 거래 내역 목록 */
        private java.util.List<Transaction> content;
        /** 현재 페이지 */
        private int page;
        /** 페이지 크기 */
        private int size;
        /** 전체 요소 수 */
        private long totalElements;
        /** 전체 페이지 수 */
        private int totalPages;
        /** 다음 페이지 존재 여부 */
        private boolean hasNext;
        /** 이전 페이지 존재 여부 */
        private boolean hasPrevious;
    }

    /**
     * 기간별 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /** 기간 내 입금 합계 */
        private int periodDepositTotal;
        /** 기간 내 환전 합계 */
        private int periodWithdrawalTotal;
        /** 순 증감 (입금 - 환전) */
        private int periodNet;
    }

    /**
     * 개별 거래 내역
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
        /** 거래 ID */
        private String txId;
        /** 거래 일시 */
        private String transactedAt;
        /** 거래 유형 */
        private String type;
        /** 거래 유형 텍스트 */
        private String typeText;
        /** 입금액 */
        private int depositAmount;
        /** 환전액 */
        private int withdrawalAmount;
        /** 거래 후 잔액 */
        private int balanceAfter;
        /** 거래 방법 */
        private String method;
        /** 거래 방법 텍스트 */
        private String methodText;
        /** 상태 */
        private String status;
        /** 메모 */
        private String note;
    }
}
