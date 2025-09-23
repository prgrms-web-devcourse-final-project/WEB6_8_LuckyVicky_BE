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
 * Builder 패턴과 복잡한 중첩 구조의 정확성을 검증
 * 2025.09.22 생성
 */
@DisplayName("ArtistMainResponse DTO 테스트")
public class ArtistMainResponseTest {

    @Test
    @DisplayName("Profile Builder 패턴 테스트")
    void builder_Profile_Success() {
        // When
        ArtistMainResponse.Profile profile = ArtistMainResponse.Profile.builder()
                .userId(12345L)
                .nickname("아티스트테스트")
                .email("artist@example.com")
                .profileImageUrl("https://cdn.example.com/artist/profile.jpg")
                .build();

        // Then
        assertAll(
                () -> assertThat(profile.getUserId()).isEqualTo(12345L),
                () -> assertThat(profile.getNickname()).isEqualTo("아티스트테스트"),
                () -> assertThat(profile.getEmail()).isEqualTo("artist@example.com"),
                () -> assertThat(profile.getProfileImageUrl()).isEqualTo("https://cdn.example.com/artist/profile.jpg")
        );
    }

    @Test
    @DisplayName("Stats Builder 패턴 테스트")
    void builder_Stats_Success() {
        // When
        ArtistMainResponse.Stats stats = ArtistMainResponse.Stats.builder()
                .followerCount(1500)
                .productCount(25)
                .todaysSales(150000)
                .todaysOrders(8)
                .totalSales(5000000)
                .totalOrders(350)
                .averageRating(4.7)
                .pendingOrders(3)
                .build();

        // Then
        assertAll(
                () -> assertThat(stats.getFollowerCount()).isEqualTo(1500),
                () -> assertThat(stats.getProductCount()).isEqualTo(25),
                () -> assertThat(stats.getTodaysSales()).isEqualTo(150000),
                () -> assertThat(stats.getTodaysOrders()).isEqualTo(8),
                () -> assertThat(stats.getTotalSales()).isEqualTo(5000000),
                () -> assertThat(stats.getTotalOrders()).isEqualTo(350),
                () -> assertThat(stats.getAverageRating()).isEqualTo(4.7),
                () -> assertThat(stats.getPendingOrders()).isEqualTo(3)
        );
    }

    @Test
    @DisplayName("DataPoint Builder 패턴 테스트")
    void builder_DataPoint_Success() {
        // When
        ArtistMainResponse.DataPoint dataPoint = ArtistMainResponse.DataPoint.builder()
                .t("2025-09-22T10:00:00")
                .v(50000)
                .build();

        // Then
        assertAll(
                () -> assertThat(dataPoint.getT()).isEqualTo("2025-09-22T10:00:00"),
                () -> assertThat(dataPoint.getV()).isEqualTo(50000)
        );
    }

    @Test
    @DisplayName("SeriesData Builder 패턴 테스트")
    void builder_SeriesData_Success() {
        // Given
        List<ArtistMainResponse.DataPoint> dataPoints = Arrays.asList(
                ArtistMainResponse.DataPoint.builder().t("2025-09-22T10:00:00").v(50000).build(),
                ArtistMainResponse.DataPoint.builder().t("2025-09-22T11:00:00").v(75000).build(),
                ArtistMainResponse.DataPoint.builder().t("2025-09-22T12:00:00").v(60000).build()
        );

        // When
        ArtistMainResponse.SeriesData seriesData = ArtistMainResponse.SeriesData.builder()
                .unit("KRW")
                .points(dataPoints)
                .total(185000)
                .build();

        // Then
        assertAll(
                () -> assertThat(seriesData.getUnit()).isEqualTo("KRW"),
                () -> assertThat(seriesData.getPoints()).hasSize(3),
                () -> assertThat(seriesData.getTotal()).isEqualTo(185000),
                () -> assertThat(seriesData.getPoints().get(0).getV()).isEqualTo(50000)
        );
    }

    @Test
    @DisplayName("Alert Builder 패턴 테스트")
    void builder_Alert_Success() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        ArtistMainResponse.Alert alert = ArtistMainResponse.Alert.builder()
                .type("NEW_ORDER")
                .message("새로운 주문이 들어왔습니다")
                .count(5)
                .timestamp(timestamp)
                .build();

        // Then
        assertAll(
                () -> assertThat(alert.getType()).isEqualTo("NEW_ORDER"),
                () -> assertThat(alert.getMessage()).isEqualTo("새로운 주문이 들어왔습니다"),
                () -> assertThat(alert.getCount()).isEqualTo(5),
                () -> assertThat(alert.getTimestamp()).isEqualTo(timestamp)
        );
    }

    @Test
    @DisplayName("ChangeData Builder 패턴 테스트")
    void builder_ChangeData_Success() {
        // When
        ArtistMainResponse.ChangeData changeData = ArtistMainResponse.ChangeData.builder()
                .delta(25000)
                .rate(15.5)
                .build();

        // Then
        assertAll(
                () -> assertThat(changeData.getDelta()).isEqualTo(25000),
                () -> assertThat(changeData.getRate()).isEqualTo(15.5)
        );
    }

    @Test
    @DisplayName("전체 응답 구조 Builder 패턴 테스트")
    void builder_MainResponse_Success() {
        // Given
        LocalDateTime serverTime = LocalDateTime.now();
        
        ArtistMainResponse.Profile profile = ArtistMainResponse.Profile.builder()
                .userId(10001L)
                .nickname("테스트아티스트")
                .email("test@artist.com")
                .profileImageUrl("https://cdn.test.com/profile.jpg")
                .build();

        ArtistMainResponse.Stats stats = ArtistMainResponse.Stats.builder()
                .followerCount(2000)
                .productCount(30)
                .todaysSales(200000)
                .todaysOrders(10)
                .totalSales(8000000)
                .totalOrders(500)
                .averageRating(4.8)
                .pendingOrders(5)
                .build();

        List<ArtistMainResponse.Alert> orderAlerts = Arrays.asList(
                ArtistMainResponse.Alert.builder()
                        .type("NEW_ORDER")
                        .message("새 주문")
                        .count(3)
                        .timestamp(serverTime)
                        .build()
        );

        ArtistMainResponse.Notifications notifications = ArtistMainResponse.Notifications.builder()
                .orderAlerts(orderAlerts)
                .fundingAlerts(Arrays.asList())
                .build();

        // When
        ArtistMainResponse response = ArtistMainResponse.builder()
                .profile(profile)
                .stats(stats)
                .notifications(notifications)
                .serverTime(serverTime)
                .timezone("Asia/Seoul")
                .build();

        // Then
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getProfile()).isNotNull(),
                () -> assertThat(response.getStats()).isNotNull(),
                () -> assertThat(response.getNotifications()).isNotNull(),
                () -> assertThat(response.getProfile().getUserId()).isEqualTo(10001L),
                () -> assertThat(response.getStats().getFollowerCount()).isEqualTo(2000),
                () -> assertThat(response.getNotifications().getOrderAlerts()).hasSize(1),
                () -> assertThat(response.getServerTime()).isEqualTo(serverTime),
                () -> assertThat(response.getTimezone()).isEqualTo("Asia/Seoul")
        );
    }

    @Test
    @DisplayName("복잡한 중첩 구조 테스트 - Meta와 Compare")
    void builder_MetaAndCompare_Success() {
        // Given
        ArtistMainResponse.Compare compare = ArtistMainResponse.Compare.builder()
                .from("2025-09-15")
                .to("2025-09-21")
                .build();

        // When
        ArtistMainResponse.Meta meta = ArtistMainResponse.Meta.builder()
                .range("week")
                .from("2025-09-22")
                .to("2025-09-28")
                .interval("day")
                .timezone("Asia/Seoul")
                .maxPoints(100)
                .compare(compare)
                .build();

        // Then
        assertAll(
                () -> assertThat(meta.getRange()).isEqualTo("week"),
                () -> assertThat(meta.getFrom()).isEqualTo("2025-09-22"),
                () -> assertThat(meta.getTo()).isEqualTo("2025-09-28"),
                () -> assertThat(meta.getInterval()).isEqualTo("day"),
                () -> assertThat(meta.getTimezone()).isEqualTo("Asia/Seoul"),
                () -> assertThat(meta.getMaxPoints()).isEqualTo(100),
                () -> assertThat(meta.getCompare()).isNotNull(),
                () -> assertThat(meta.getCompare().getFrom()).isEqualTo("2025-09-15"),
                () -> assertThat(meta.getCompare().getTo()).isEqualTo("2025-09-21")
        );
    }

    @Test
    @DisplayName("API 명세와 동일한 구조 생성 테스트")
    void createApiResponseStructure() {
        // Given
        LocalDateTime serverTime = LocalDateTime.of(2025, 9, 22, 15, 0);

        // When
        ArtistMainResponse response = ArtistMainResponse.builder()
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
                .serverTime(serverTime)
                .timezone("Asia/Seoul")
                .build();

        // Then
        assertAll(
                () -> assertThat(response.getProfile().getUserId()).isEqualTo(20001L),
                () -> assertThat(response.getProfile().getNickname()).isEqualTo("프로아티스트"),
                () -> assertThat(response.getStats().getFollowerCount()).isEqualTo(5000),
                () -> assertThat(response.getStats().getTotalSales()).isEqualTo(50000000),
                () -> assertThat(response.getStats().getAverageRating()).isEqualTo(4.9),
                () -> assertThat(response.getServerTime()).isEqualTo(serverTime),
                () -> assertThat(response.getTimezone()).isEqualTo("Asia/Seoul")
        );
    }
}
