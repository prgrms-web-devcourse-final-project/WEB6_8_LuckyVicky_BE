package com.back.domain.dashboard.artist.dto;

/**
 * 작가 대시보드 통계 DTO
 * - 한 번의 쿼리로 모든 통계 조회
 */
public record DashboardStatsDto(
        Long todaysOrderCount,    // 오늘의 주문 건수
        Long todaysSales,         // 오늘의 매출
        Long totalOrderCount,     // 총 주문 건수
        Long totalSales           // 총 매출
) {
    /**
     * 기본값 생성자 (데이터 없을 때)
     */
    public static DashboardStatsDto empty() {
        return new DashboardStatsDto(0L, 0L, 0L, 0L);
    }
}
