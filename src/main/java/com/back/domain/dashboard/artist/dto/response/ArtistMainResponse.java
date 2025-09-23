package com.back.domain.dashboard.artist.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작가 대시보드 메인 현황 응답 DTO
 *
 * 작가의 프로필, 통계, 트렌드, 알림 정보를 포함
 * 2025.09.23 생성
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistMainResponse {

    /** 작가 프로필 정보 */
    private Profile profile;
    /** 통계 정보 */
    private Stats stats;
    /** 트렌드 정보 */
    private Trends trends;
    /** 알림 정보 */
    private Notifications notifications;
    /** 서버 시간 */
    private LocalDateTime serverTime;
    /** 타임존 */
    private String timezone;

    /**
     * 작가 프로필 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        /** 사용자 ID */
        private Long userId;
        /** 닉네임 */
        private String nickname;
        /** 이메일 */
        private String email;
        /** 프로필 이미지 URL */
        private String profileImageUrl;
    }

    /**
     * 통계 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        /** 팔로워 수 */
        private int followerCount;
        /** 상품 수 */
        private int productCount;
        /** 오늘 매출 */
        private int todaysSales;
        /** 오늘 주문 수 */
        private int todaysOrders;
        /** 총 매출 */
        private int totalSales;
        /** 총 주문 수 */
        private int totalOrders;
        /** 평균 평점 */
        private double averageRating;
        /** 대기중인 주문 수 */
        private int pendingOrders;
    }

    /**
     * 트렌드 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trends {
        /** 메타 정보 */
        private Meta meta;
        /** 시계열 데이터 */
        private Series series;
        /** 변화량 정보 */
        private Changes changes;
    }

    /**
     * 트렌드 메타 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        /** 범위 */
        private String range;
        /** 시작일 */
        private String from;
        /** 종료일 */
        private String to;
        /** 간격 */
        private String interval;
        /** 타임존 */
        private String timezone;
        /** 최대 포인트 수 */
        private int maxPoints;
        /** 비교 기간 */
        private Compare compare;
    }

    /**
     * 비교 기간 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Compare {
        /** 비교 시작일 */
        private String from;
        /** 비교 종료일 */
        private String to;
    }

    /**
     * 시계열 데이터
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Series {
        /** 매출 데이터 */
        private SeriesData sales;
        /** 주문 데이터 */
        private SeriesData orders;
        /** 팔로워 데이터 */
        private SeriesData followers;
    }

    /**
     * 시계열 개별 데이터
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesData {
        /** 단위 */
        private String unit;
        /** 데이터 포인트 */
        private List<DataPoint> points;
        /** 총합 */
        private int total;
    }

    /**
     * 데이터 포인트
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        /** 시간 */
        private String t;
        /** 값 */
        private int v;
    }

    /**
     * 변화량 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Changes {
        /** 매출 변화 */
        private ChangeData sales;
        /** 주문 변화 */
        private ChangeData orders;
        /** 팔로워 변화 */
        private ChangeData followers;
    }

    /**
     * 개별 변화 데이터
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeData {
        /** 변화량 */
        private int delta;
        /** 변화율 */
        private double rate;
    }

    /**
     * 알림 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notifications {
        /** 주문 알림 */
        private List<Alert> orderAlerts;
        /** 펀딩 알림 */
        private List<Alert> fundingAlerts;
    }

    /**
     * 알림 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        /** 알림 타입 */
        private String type;
        /** 메시지 */
        private String message;
        /** 개수 */
        private int count;
        /** 타임스탬프 */
        private LocalDateTime timestamp;
    }
}