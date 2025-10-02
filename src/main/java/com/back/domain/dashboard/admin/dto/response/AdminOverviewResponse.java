package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 대시보드 전체 현황 응답 DTO
 *
 * 관리자 대시보드의 전체 통계, 차트, 알림, 유입 경로 정보를 포함
 * 2025.10.01 수정 - GA4 유입 경로 추가
 */
public record AdminOverviewResponse(
        /** 전체 현황 통계 */
        Overview overview,
        /** 차트 데이터 */
        Charts charts,
        /** 승인 대기 알림 */
        Alerts alerts,
        /** 유입 경로 정보 (GA4) */
        TrafficSources trafficSources,
        /** 서버 시간 */
        LocalDateTime serverTime,
        /** 타임존 */
        String timezone
) {

    /**
     * 전체 현황 통계
     */
    public record Overview(
            /** 가입자 수 */
            StatInfo userCount,
            /** 주문 통계 */
            StatInfo orderStats,
            /** 매출 통계 */
            StatInfo salesStats,
            /** 상품 수 */
            StatInfo productCount,
            /** 펀딩 수 */
            StatInfo fundingCount,
            /** 작가 수 */
            StatInfo artistCount
    ) {}

    /**
     * 통계 정보
     */
    public record StatInfo(
            /** 수치 */
            long count,
            /** 라벨 */
            String label,
            /** 단위 */
            String unit,
            /** 변화량 */
            long delta,
            /** 변화율 */
            double rate
    ) {}

    /**
     * 차트 데이터
     */
    public record Charts(
            /** 메타 정보 */
            ChartMeta meta,
            /** 매출 트렌드 */
            SalesTrend salesTrend,
            /** 사용자 증가 현황 */
            UserGrowth userGrowth,
            /** 카테고리별 분포 */
            CategoryDistribution categoryDistribution
    ) {}

    /**
     * 차트 메타 정보
     */
    public record ChartMeta(
            /** 범위 */
            String range,
            /** 집계 단위 */
            String granularity,
            /** 타임존 */
            String timezone
    ) {}

    /**
     * 매출 트렌드
     */
    public record SalesTrend(
            /** 시계열 데이터 */
            SalesSeries series,
            /** 변화량 정보 */
            SalesDelta delta
    ) {}

    /**
     * 매출/주문 시계열 데이터
     */
    public record SalesSeries(
            /** 매출 데이터 */
            List<DataPoint> sales,
            /** 주문 데이터 */
            List<DataPoint> orders
    ) {}

    /**
     * 매출/주문 변화량
     */
    public record SalesDelta(
            /** 매출 변화 */
            DeltaInfo sales,
            /** 주문 변화 */
            DeltaInfo orders
    ) {}

    /**
     * 사용자 증가 현황
     */
    public record UserGrowth(
            /** 시계열 데이터 */
            UserSeries series,
            /** 변화량 정보 */
            UserDelta delta
    ) {}

    /**
     * 사용자/작가 시계열 데이터
     */
    public record UserSeries(
            /** 사용자 데이터 */
            List<DataPoint> users,
            /** 작가 데이터 */
            List<DataPoint> artists
    ) {}

    /**
     * 사용자/작가 변화량
     */
    public record UserDelta(
            /** 사용자 변화 */
            DeltaInfo users,
            /** 작가 변화 */
            DeltaInfo artists
    ) {}

    /**
     * 카테고리별 분포
     */
    public record CategoryDistribution(
            /** 기준일 */
            String asOf,
            /** 총 상품 수 */
            int totalProducts,
            /** 카테고리별 데이터 */
            List<CategoryBucket> buckets
    ) {}

    /**
     * 카테고리 버킷
     */
    public record CategoryBucket(
            /** 카테고리 ID */
            Long categoryId,
            /** 카테고리명 */
            String name,
            /** 상품 수 */
            int count,
            /** 점유율 */
            double share
    ) {}

    /**
     * 데이터 포인트
     */
    public record DataPoint(
            /** 날짜 */
            String date,
            /** 값 */
            long value
    ) {}

    /**
     * 변화량 정보
     */
    public record DeltaInfo(
            /** 변화량 */
            long delta,
            /** 변화율 */
            double rate
    ) {}

    /**
     * 승인 대기 알림
     */
    public record Alerts(
            /** 작가 승인 대기 */
            List<ArtistApproval> artistApprovalPending,
            /** 펀딩 승인 대기 */
            List<FundingApproval> fundingApprovalPending
    ) {}

    /**
     * 작가 승인 정보
     */
    public record ArtistApproval(
            /** 작가 ID */
            Long artistId,
            /** 닉네임 */
            String nickname,
            /** 요청 시간 */
            LocalDateTime requestedAt
    ) {}

    /**
     * 펀딩 승인 정보
     */
    public record FundingApproval(
            /** 펀딩 ID */
            Long fundingId,
            /** 상품명 */
            String productName,
            /** 요청 시간 */
            LocalDateTime requestedAt
    ) {}

    /**
     * 유입 경로 정보 (GA4)
     */
    public record TrafficSources(
            /** 요약 정보 */
            Summary summary,
            /** 유입 경로별 데이터 */
            List<Source> sources,
            /** 차트용 데이터 */
            Chart chart
    ) {}

    /**
     * 유입 경로 요약 정보
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
            /** 유입 경로명 (Instagram, YouTube, Naver 등) */
            String name,
            /** 세션 수 */
            long sessions,
            /** 사용자 수 */
            long users,
            /** 점유율 (%) */
            double share
    ) {}

    /**
     * 파이차트 데이터
     */
    public record Chart(
            /** 차트 데이터 */
            List<ChartData> data
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
}
