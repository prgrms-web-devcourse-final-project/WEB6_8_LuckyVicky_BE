package com.back.domain.dashboard.artist.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작가 대시보드 메인 현황 응답 DTO
 *
 * 작가의 프로필, 통계, 트렌드, 알림, 유입 경로 정보를 포함
 * 2025.10.01 수정 - GA4 유입 경로 추가
 */
public record ArtistMainResponse(
        /** 작가 프로필 정보 */
        Profile profile,
        /** 통계 정보 */
        Stats stats,
        /** 트렌드 정보 */
        Trends trends,
        /** 알림 정보 */
        Notifications notifications,
        /** 유입 경로 정보 (GA4) */
        TrafficSources trafficSources,
        /** 서버 시간 */
        LocalDateTime serverTime,
        /** 타임존 */
        String timezone
) {

    /**
     * 작가 프로필 정보
     */
    public record Profile(
            /** 사용자 ID */
            Long userId,
            /** 닉네임 */
            String nickname,
            /** 이메일 */
            String email,
            /** 프로필 이미지 URL */
            String profileImageUrl
    ) {}

    /**
     * 통계 정보
     */
    public record Stats(
            /** 팔로워 수 */
            int followerCount,
            /** 상품 수 */
            int productCount,
            /** 오늘 매출 */
            int todaysSales,
            /** 오늘 주문 수 */
            int todaysOrders,
            /** 총 매출 */
            int totalSales,
            /** 총 주문 수 */
            int totalOrders,
            /** 평균 평점 */
            double averageRating,
            /** 대기중인 주문 수 */
            int pendingOrders
    ) {}

    /**
     * 트렌드 정보
     */
    public record Trends(
            /** 메타 정보 */
            Meta meta,
            /** 시계열 데이터 */
            Series series,
            /** 변화량 정보 */
            Changes changes
    ) {}

    /**
     * 트렌드 메타 정보
     */
    public record Meta(
            /** 범위 */
            String range,
            /** 시작일 */
            String from,
            /** 종료일 */
            String to,
            /** 간격 */
            String interval,
            /** 타임존 */
            String timezone,
            /** 최대 포인트 수 */
            int maxPoints,
            /** 비교 기간 */
            Compare compare
    ) {}

    /**
     * 비교 기간 정보
     */
    public record Compare(
            /** 비교 시작일 */
            String from,
            /** 비교 종료일 */
            String to
    ) {}

    /**
     * 시계열 데이터
     */
    public record Series(
            /** 매출 데이터 */
            SeriesData sales,
            /** 주문 데이터 */
            SeriesData orders,
            /** 팔로워 데이터 */
            SeriesData followers
    ) {}

    /**
     * 시계열 개별 데이터
     */
    public record SeriesData(
            /** 단위 */
            String unit,
            /** 데이터 포인트 */
            List<DataPoint> points,
            /** 총합 */
            int total
    ) {}

    /**
     * 데이터 포인트
     */
    public record DataPoint(
            /** 시간 */
            String t,
            /** 값 */
            int v
    ) {}

    /**
     * 변화량 정보
     */
    public record Changes(
            /** 매출 변화 */
            ChangeData sales,
            /** 주문 변화 */
            ChangeData orders,
            /** 팔로워 변화 */
            ChangeData followers
    ) {}

    /**
     * 개별 변화 데이터
     */
    public record ChangeData(
            /** 변화량 */
            int delta,
            /** 변화율 */
            double rate
    ) {}

    /**
     * 알림 정보
     */
    public record Notifications(
            /** 주문 알림 */
            List<Alert> orderAlerts,
            /** 펀딩 알림 */
            List<Alert> fundingAlerts
    ) {}

    /**
     * 알림 정보
     */
    public record Alert(
            /** 알림 타입 */
            String type,
            /** 메시지 */
            String message,
            /** 개수 */
            int count,
            /** 타임스탬프 */
            LocalDateTime timestamp
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
