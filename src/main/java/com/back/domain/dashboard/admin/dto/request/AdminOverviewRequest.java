package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Pattern;

/**
 * 관리자 대시보드 전체 현황 조회 요청 DTO
 * 2025.09.26 신규 생성
 */
public record AdminOverviewRequest(
        /** 조회 범위 */
        @Pattern(regexp = "^(1M|3M|6M|1Y|ALL)$",
                message = "range는 1M, 3M, 6M, 1Y, ALL 중 하나여야 합니다")
        String range,

        /** 집계 단위 */
        @Pattern(regexp = "^(DAY|WEEK|MONTH)$",
                message = "granularity는 DAY, WEEK, MONTH 중 하나여야 합니다")
        String granularity,

        /** 백워드 호환용 기간 */
        @Pattern(regexp = "^(WEEK|MONTH|QUARTER|YEAR)$",
                message = "period는 WEEK, MONTH, QUARTER, YEAR 중 하나여야 합니다")
        String period,

        /** 서버 집계 기준 타임존 */
        String timezone
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminOverviewRequest {
        if (range == null) range = "1M";
        if (granularity == null) granularity = "DAY";
        if (period == null) period = "MONTH";
        if (timezone == null) timezone = "Asia/Seoul";
    }
}
