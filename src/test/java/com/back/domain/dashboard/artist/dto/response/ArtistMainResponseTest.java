package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistMainResponse DTO 테스트
 * 전체 구조와 핵심 비즈니스 로직에 집중
 * 2025.09.23 생성
 */
@DisplayName("ArtistMainResponse DTO 테스트")
public class ArtistMainResponseTest {

    @Test
    @DisplayName("전체 응답 구조 생성 및 검증")
    void createCompleteResponse_Success() {
        // Given
        LocalDateTime serverTime = LocalDateTime.now();
        
        // When
        ArtistMainResponse response = createSampleResponse(serverTime);

        // Then - 핵심 구조만 검증
        assertAll(
                () -> assertThat(response.getProfile()).isNotNull(),
                () -> assertThat(response.getStats()).isNotNull(),
                () -> assertThat(response.getTrends()).isNotNull(),
                () -> assertThat(response.getNotifications()).isNotNull(),
                () -> assertThat(response.getServerTime()).isEqualTo(serverTime),
                () -> assertThat(response.getTimezone()).isEqualTo("Asia/Seoul")
        );
    }

    @Test
    @DisplayName("중첩된 객체 구조 검증")
    void validateNestedStructure_Success() {
        // When
        ArtistMainResponse response = createSampleResponse(LocalDateTime.now());

        // Then - 중요한 중첩 구조만 검증
        assertAll(
                () -> assertThat(response.getTrends().getMeta().getCompare()).isNotNull(),
                () -> assertThat(response.getTrends().getSeries().getSales().getPoints()).isNotEmpty(),
                () -> assertThat(response.getTrends().getChanges().getSales().getDelta()).isNotNull(),
                () -> assertThat(response.getNotifications().getOrderAlerts()).isNotEmpty()
        );
    }

    @Test
    @DisplayName("시계열 데이터 구조 검증")
    void validateTimeSeriesData_Success() {
        // When
        ArtistMainResponse response = createSampleResponse(LocalDateTime.now());
        ArtistMainResponse.SeriesData salesData = response.getTrends().getSeries().getSales();

        // Then - 시계열 데이터 구조 검증
        assertAll(
                () -> assertThat(salesData.getUnit()).isEqualTo("KRW"),
                () -> assertThat(salesData.getPoints()).hasSize(3),
                () -> assertThat(salesData.getTotal()).isPositive(),
                () -> assertThat(salesData.getPoints().get(0).getT()).isNotBlank(),
                () -> assertThat(salesData.getPoints().get(0).getV()).isNotNegative()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistMainResponse response = createApiResponse();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.getProfile().getUserId()).isPositive(),
                () -> assertThat(response.getProfile().getNickname()).isNotBlank(),
                () -> assertThat(response.getStats().getTotalSales()).isNotNegative(),
                () -> assertThat(response.getTrends().getMeta().getRange()).isNotBlank(),
                () -> assertThat(response.getNotifications().getOrderAlerts()).isNotNull(),
                () -> assertThat(response.getNotifications().getFundingAlerts()).isNotNull()
        );
    }

    // ---------- 헬퍼 메서드들 -------------

    private ArtistMainResponse createSampleResponse(LocalDateTime serverTime) {
        return ArtistMainResponse.builder()
                .profile(createSampleProfile())
                .stats(createSampleStats())
                .trends(createSampleTrends())
                .notifications(createSampleNotifications())
                .serverTime(serverTime)
                .timezone("Asia/Seoul")
                .build();
    }

    private ArtistMainResponse createApiResponse() {
        return ArtistMainResponse.builder()
                .profile(ArtistMainResponse.Profile.builder()
                        .userId(20001L)
                        .nickname("프로아티스트")
                        .email("pro@artist.example.com")
                        .profileImageUrl("https://cdn.example.com/u/20001/profile.jpg")
                        .build())
                .stats(ArtistMainResponse.Stats.builder()
                        .followerCount(5000)
                        .productCount(75)
                        .todaysSales(1000000)
                        .todaysOrders(25)
                        .totalSales(50000000)
                        .totalOrders(2000)
                        .averageRating(4.9)
                        .pendingOrders(12)
                        .build())
                .trends(createSampleTrends())
                .notifications(createSampleNotifications())
                .serverTime(LocalDateTime.of(2025, 9, 22, 15, 0))
                .timezone("Asia/Seoul")
                .build();
    }

    private ArtistMainResponse.Profile createSampleProfile() {
        return ArtistMainResponse.Profile.builder()
                .userId(1L)
                .nickname("테스트작가")
                .email("test@example.com")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
    }

    private ArtistMainResponse.Stats createSampleStats() {
        return ArtistMainResponse.Stats.builder()
                .followerCount(1500)
                .productCount(25)
                .todaysSales(150000)
                .todaysOrders(8)
                .totalSales(5000000)
                .totalOrders(350)
                .averageRating(4.7)
                .pendingOrders(3)
                .build();
    }

    private ArtistMainResponse.Trends createSampleTrends() {
        return ArtistMainResponse.Trends.builder()
                .meta(createSampleMeta())
                .series(createSampleSeries())
                .changes(createSampleChanges())
                .build();
    }

    private ArtistMainResponse.Meta createSampleMeta() {
        return ArtistMainResponse.Meta.builder()
                .range("6M")
                .from("2025-03-01")
                .to("2025-09-01")
                .interval("WEEK")
                .timezone("Asia/Seoul")
                .maxPoints(400)
                .compare(ArtistMainResponse.Compare.builder()
                        .from("2024-09-01")
                        .to("2025-03-01")
                        .build())
                .build();
    }

    private ArtistMainResponse.Series createSampleSeries() {
        List<ArtistMainResponse.DataPoint> dataPoints = Arrays.asList(
                ArtistMainResponse.DataPoint.builder().t("2025-09-22T10:00:00").v(50000).build(),
                ArtistMainResponse.DataPoint.builder().t("2025-09-22T11:00:00").v(75000).build(),
                ArtistMainResponse.DataPoint.builder().t("2025-09-22T12:00:00").v(60000).build()
        );

        return ArtistMainResponse.Series.builder()
                .sales(ArtistMainResponse.SeriesData.builder()
                        .unit("KRW")
                        .points(dataPoints)
                        .total(185000)
                        .build())
                .orders(ArtistMainResponse.SeriesData.builder()
                        .unit("COUNT")
                        .points(Arrays.asList())
                        .total(156)
                        .build())
                .followers(ArtistMainResponse.SeriesData.builder()
                        .unit("COUNT")
                        .points(Arrays.asList())
                        .total(1250)
                        .build())
                .build();
    }

    private ArtistMainResponse.Changes createSampleChanges() {
        return ArtistMainResponse.Changes.builder()
                .sales(ArtistMainResponse.ChangeData.builder().delta(25000).rate(15.5).build())
                .orders(ArtistMainResponse.ChangeData.builder().delta(1).rate(0.143).build())
                .followers(ArtistMainResponse.ChangeData.builder().delta(70).rate(0.059).build())
                .build();
    }

    private ArtistMainResponse.Notifications createSampleNotifications() {
        List<ArtistMainResponse.Alert> orderAlerts = Arrays.asList(
                ArtistMainResponse.Alert.builder()
                        .type("NEW_ORDER")
                        .message("새 주문")
                        .count(3)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        return ArtistMainResponse.Notifications.builder()
                .orderAlerts(orderAlerts)
                .fundingAlerts(Arrays.asList())
                .build();
    }
}
