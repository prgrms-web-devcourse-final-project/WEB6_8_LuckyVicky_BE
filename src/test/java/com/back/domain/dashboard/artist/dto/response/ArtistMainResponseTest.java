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
 * 2025.09.25 수정
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
                () -> assertThat(response.profile()).isNotNull(),
                () -> assertThat(response.stats()).isNotNull(),
                () -> assertThat(response.trends()).isNotNull(),
                () -> assertThat(response.notifications()).isNotNull(),
                () -> assertThat(response.serverTime()).isEqualTo(serverTime),
                () -> assertThat(response.timezone()).isEqualTo("Asia/Seoul")
        );
    }

    @Test
    @DisplayName("중첩된 객체 구조 검증")
    void validateNestedStructure_Success() {
        // When
        ArtistMainResponse response = createSampleResponse(LocalDateTime.now());

        // Then - 중요한 중첩 구조만 검증
        assertAll(
                () -> assertThat(response.trends().meta().compare()).isNotNull(),
                () -> assertThat(response.trends().series().sales().points()).isNotEmpty(),
                () -> assertThat(response.trends().changes().sales().delta()).isNotNull(),
                () -> assertThat(response.notifications().orderAlerts()).isNotEmpty()
        );
    }

    @Test
    @DisplayName("시계열 데이터 구조 검증")
    void validateTimeSeriesData_Success() {
        // When
        ArtistMainResponse response = createSampleResponse(LocalDateTime.now());
        ArtistMainResponse.SeriesData salesData = response.trends().series().sales();

        // Then - 시계열 데이터 구조 검증
        assertAll(
                () -> assertThat(salesData.unit()).isEqualTo("KRW"),
                () -> assertThat(salesData.points()).hasSize(3),
                () -> assertThat(salesData.total()).isPositive(),
                () -> assertThat(salesData.points().get(0).t()).isNotBlank(),
                () -> assertThat(salesData.points().get(0).v()).isNotNegative()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistMainResponse response = createApiResponse();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.profile().userId()).isPositive(),
                () -> assertThat(response.profile().nickname()).isNotBlank(),
                () -> assertThat(response.stats().totalSales()).isNotNegative(),
                () -> assertThat(response.trends().meta().range()).isNotBlank(),
                () -> assertThat(response.notifications().orderAlerts()).isNotNull(),
                () -> assertThat(response.notifications().fundingAlerts()).isNotNull()
        );
    }

    // ---------- 헬퍼 메서드들 -------------

    private ArtistMainResponse createSampleResponse(LocalDateTime serverTime) {
        return new ArtistMainResponse(
                createSampleProfile(),
                createSampleStats(),
                createSampleTrends(),
                createSampleNotifications(),
                serverTime,
                "Asia/Seoul"
        );
    }

    private ArtistMainResponse createApiResponse() {
        return new ArtistMainResponse(
                new ArtistMainResponse.Profile(20001L, "프로아티스트", "pro@artist.example.com", "https://cdn.example.com/u/20001/profile.jpg"),
                new ArtistMainResponse.Stats(5000, 75, 1000000, 25, 50000000, 2000, 4.9, 12),
                createSampleTrends(),
                createSampleNotifications(),
                LocalDateTime.of(2025, 9, 22, 15, 0),
                "Asia/Seoul"
        );
    }

    private ArtistMainResponse.Profile createSampleProfile() {
        return new ArtistMainResponse.Profile(1L, "테스트작가", "test@example.com", "https://example.com/profile.jpg");
    }

    private ArtistMainResponse.Stats createSampleStats() {
        return new ArtistMainResponse.Stats(1500, 25, 150000, 8, 5000000, 350, 4.7, 3);
    }

    private ArtistMainResponse.Trends createSampleTrends() {
        return new ArtistMainResponse.Trends(createSampleMeta(), createSampleSeries(), createSampleChanges());
    }

    private ArtistMainResponse.Meta createSampleMeta() {
        return new ArtistMainResponse.Meta(
                "6M", "2025-03-01", "2025-09-01", "WEEK", "Asia/Seoul", 400,
                new ArtistMainResponse.Compare("2024-09-01", "2025-03-01")
        );
    }

    private ArtistMainResponse.Series createSampleSeries() {
        List<ArtistMainResponse.DataPoint> dataPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-09-22T10:00:00", 50000),
                new ArtistMainResponse.DataPoint("2025-09-22T11:00:00", 75000),
                new ArtistMainResponse.DataPoint("2025-09-22T12:00:00", 60000)
        );

        return new ArtistMainResponse.Series(
                new ArtistMainResponse.SeriesData("KRW", dataPoints, 185000),
                new ArtistMainResponse.SeriesData("COUNT", Arrays.asList(), 156),
                new ArtistMainResponse.SeriesData("COUNT", Arrays.asList(), 1250)
        );
    }

    private ArtistMainResponse.Changes createSampleChanges() {
        return new ArtistMainResponse.Changes(
                new ArtistMainResponse.ChangeData(25000, 15.5),
                new ArtistMainResponse.ChangeData(1, 0.143),
                new ArtistMainResponse.ChangeData(70, 0.059)
        );
    }

    private ArtistMainResponse.Notifications createSampleNotifications() {
        List<ArtistMainResponse.Alert> orderAlerts = Arrays.asList(
                new ArtistMainResponse.Alert("NEW_ORDER", "새 주문", 3, LocalDateTime.now())
        );

        return new ArtistMainResponse.Notifications(orderAlerts, Arrays.asList());
    }
}
