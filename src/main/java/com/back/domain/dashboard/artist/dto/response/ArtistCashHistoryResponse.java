package com.back.domain.dashboard.artist.dto.response;

/**
 * 작가 캐시 입금/환전 내역 응답 DTO
 * 
 * 작가의 캐시 거래 내역 정보를 포함
 * 2025.09.25 수정
 */
public class ArtistCashHistoryResponse {

    /**
     * 캐시 내역 목록 응답
     */
    public record List(
            /** 요약 정보 */
            Summary summary,
            /** 거래 내역 목록 */
            java.util.List<Transaction> content,
            /** 현재 페이지 */
            int page,
            /** 페이지 크기 */
            int size,
            /** 전체 요소 수 */
            long totalElements,
            /** 전체 페이지 수 */
            int totalPages,
            /** 다음 페이지 존재 여부 */
            boolean hasNext,
            /** 이전 페이지 존재 여부 */
            boolean hasPrevious
    ) {}

    /**
     * 기간별 요약 정보
     */
    public record Summary(
            /** 기간 내 입금 합계 */
            int periodDepositTotal,
            /** 기간 내 환전 합계 */
            int periodWithdrawalTotal,
            /** 순 증감 (입금 - 환전) */
            int periodNet
    ) {}

    /**
     * 개별 거래 내역
     */
    public record Transaction(
            /** 거래 ID */
            String txId,
            /** 거래 일시 */
            String transactedAt,
            /** 거래 유형 */
            String type,
            /** 거래 유형 텍스트 */
            String typeText,
            /** 입금액 */
            int depositAmount,
            /** 환전액 */
            int withdrawalAmount,
            /** 거래 후 잔액 */
            int balanceAfter,
            /** 거래 방법 */
            String method,
            /** 거래 방법 텍스트 */
            String methodText,
            /** 상태 */
            String status,
            /** 메모 */
            String note
    ) {}
}
