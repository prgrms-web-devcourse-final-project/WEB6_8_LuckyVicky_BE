package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 대시보드 유입 경로 응답 DTO
 * 
 * GA4 API를 통해 수집한 소셜 미디어 및 검색 엔진 유입 경로 데이터
 * 2025.10.01 생성
 */
public record AdminTrafficSourceResponse(
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
            /** 평균 세션 시간 (초) */
            double avgSessionDuration,
            /** 이탈률 (%) */
            double bounceRate
    ) {}

    /**
     * 유입 경로 정보
     */
    public record Source(
            /** 유입 경로명 (Instagram, YouTube, Naver, Google 등) */
            String name,
            /** 세션 수 */
            long sessions,
            /** 사용자 수 */
            long users,
            /** 점유율 (%) */
            double share,
            /** 신규 사용자 비율 (%) */
            double newUserRate,
            /** 이탈률 (%) */
            double bounceRate,
            /** 평균 세션 시간 (초) */
            double avgSessionDuration,
            /** 전환 수 (구매, 가입 등) */
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
