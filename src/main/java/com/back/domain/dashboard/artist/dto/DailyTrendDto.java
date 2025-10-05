package com.back.domain.dashboard.artist.dto;

import java.time.LocalDate;

/**
 * 일별 트렌드 DTO
 * - 매출과 주문 수를 하나의 쿼리로 조회
 */
public record DailyTrendDto(
        LocalDate date,           // 날짜
        Long orderCount,          // 주문 수
        Long salesAmount          // 매출
) {
}
