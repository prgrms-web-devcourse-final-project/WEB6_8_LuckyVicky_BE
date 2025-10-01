package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 매출/정산 집계 조회 요청 DTO
 * 2025.09.26 신규 생성
 */
public record AdminSettlementRequest(
        /** 연도 */
        @Min(value = 2020, message = "연도는 2020년 이상이어야 합니다")
        @Max(value = 2030, message = "연도는 2030년 이하여야 합니다")
        Integer year,

        /** 월 (1-12, 전달 시 일별 집계로 전환) */
        @Min(value = 1, message = "월은 1 이상이어야 합니다")
        @Max(value = 12, message = "월은 12 이하여야 합니다")
        Integer month,

        /** 집계 단위 */
        @Pattern(regexp = "^(MONTH|DAY)$",
                message = "granularity는 MONTH, DAY 중 하나여야 합니다")
        String granularity,

        /** 집계 기준 타임존 */
        String timezone
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminSettlementRequest {
        if (granularity == null) granularity = "MONTH";
        if (timezone == null) timezone = "Asia/Seoul";
    }
}
