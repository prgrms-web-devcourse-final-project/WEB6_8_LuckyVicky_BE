package com.back.domain.dashboard.admin.dto;

import java.time.LocalDate;

/**
 * 관리자 대시보드 - 월별/일별 정산 집계 DTO
 * Order 테이블 집계 결과를 담는 DTO
 */
public record MonthlySettlementDto(
        LocalDate date,           // 집계 날짜 (월별: 1일, 일별: 해당 일)
        Long totalAmount          // 총 매출금액 (finalAmount 합계)
) {
    /**
     * 작가 정산금 계산 (총 매출의 90%)
     */
    public Long getArtistPayout() {
        return (long) (totalAmount * 0.9);
    }

    /**
     * 순수익 계산 (총 매출의 10%)
     */
    public Long getNetIncome() {
        return (long) (totalAmount * 0.1);
    }
}
