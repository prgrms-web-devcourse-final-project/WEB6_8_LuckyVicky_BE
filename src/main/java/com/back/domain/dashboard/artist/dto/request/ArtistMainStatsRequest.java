package com.back.domain.dashboard.artist.dto.request;

import jakarta.validation.constraints.Pattern;

/**
 * 작가 대시보드 메인 현황 조회 요청 DTO
 * 2025.09.25 생성
 */
public record ArtistMainStatsRequest(
        /** 조회 범위 */
        @Pattern(regexp = "^(1D|7D|30D|3M|6M|1Y|CUSTOM)$",
                message = "range는 1D, 7D, 30D, 3M, 6M, 1Y, CUSTOM 중 하나여야 합니다")
        String range,

        /** 시작일 (CUSTOM 범위일 때 필수) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String from,

        /** 종료일 (CUSTOM 범위일 때 필수) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String to,

        /** 데이터 간격 */
        @Pattern(regexp = "^(HOUR|DAY|WEEK|MONTH|AUTO)$",
                message = "interval은 HOUR, DAY, WEEK, MONTH, AUTO 중 하나여야 합니다")
        String interval,

        /** 타임존 */
        String tz
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ArtistMainStatsRequest {
        if (range == null) range = "6M";
        if (interval == null) interval = "AUTO";
        if (tz == null) tz = "Asia/Seoul";
    }
}
