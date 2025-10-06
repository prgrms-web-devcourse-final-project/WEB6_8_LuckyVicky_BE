package com.back.domain.dashboard.admin.dto;

import java.time.LocalDate;

/**
 * 관리자 대시보드 - 일별 사용자/작가 증가 DTO
 * User 테이블 집계 결과를 담는 DTO
 */
public record DailyUserGrowthDto(
        LocalDate date,           // 날짜
        Long userCount            // 사용자 수 (신규 가입자 또는 작가 수)
) {
}
