package com.back.domain.dashboard.artist.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작가 대시보드 유입 경로 응답 DTO
 * 
 * 작가 본인의 소셜 미디어 유입 경로 통계
 * 2025.10.01 생성
 */
public record ArtistTrafficSourceResponse(
        /** 요약 정보 */
        Summary summary,
        /** 유입 경로별 데이터 */
        List<Source> sources,
        /** 차트용 데이터 */
        Chart chart,
        /** 조회 기간 */
        Period period,
        /** 서버 시간 */
        LocalDateTime serverTime,
        /** 타임존 */
        String timezone
) {

    /**
     * 요약 정보
     */
    public record Summary(
            /** 총 세션 수 */
            long totalSessions,
            /** 총 사용자 수 */
            long totalUsers,
            /** 전환 수 */
            long conversions,
            /** 전환율 (%) */
            double conversionRate,
            /** 가장 많은 유입 경로 */
            String topSource
    ) {}

    /**
     * 유입 경로 정보
     */
    public record Source(
            /** 유입 경로명 (Instagram, YouTube, Naver 등) */
            String name,
            /** 세션 수 */
            long sessions,
            /** 사용자 수 */
            long users,
            /** 점유율 (%) */
            double share,
            /** 신규 사용자 수 */
            long newUsers,
            /** 신규 사용자 비율 (%) */
            double newUserRate,
            /** 전환 수 */
            long conversions,
            /** 전환율 (%) */
            double conversionRate
    ) {}

    /**
     * 파이차트 데이터
     */
    public record Chart(
            /** 차트 데이터 */
            List<ChartData> data,
            /** 기타(Others) 그룹화 임계값 (%) */
            double othersThreshold
    ) {}

    /**
     * 차트 개별 데이터
     */
    public record ChartData(
            /** 유입 경로명 */
            String name,
            /** 값 (세션 수) */
            long value,
            /** 점유율 (%) */
            double percentage,
            /** 색상 코드 (예: #FF6B6B) */
            String color
    ) {}

    /**
     * 조회 기간
     */
    public record Period(
            /** 시작일 */
            String startDate,
            /** 종료일 */
            String endDate,
            /** 조회 일수 */
            int days
    ) {}
}
