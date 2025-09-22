package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistDashboardServiceImpl 테스트
 * 
 * 작가용 대시보드 Service 레이어의 비즈니스 로직을 테스트합니다.
 * 2025.09.22
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("작가 대시보드 서비스 구현체 테스트")
class ArtistDashboardServiceImplTest {

    @InjectMocks
    private ArtistDashboardServiceImpl artistDashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Nested
    @DisplayName("작가 대시보드 메인 현황 조회 테스트")
    class GetMainStatsTest {

        @Test
        @DisplayName("메인 현황 조회 성공")
        void getMainStats_Success() {
            // Given
            String range = "6M", from = null, to = null, interval = "AUTO", tz = "Asia/Seoul";

            // When
            ArtistMainResponse result = artistDashboardService.getMainStats(
                    TEST_AUTHORIZATION, range, from, to, interval, tz);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getProfile()).isNotNull(),
                    () -> assertThat(result.getStats()).isNotNull(),
                    () -> assertThat(result.getTrends()).isNotNull(),
                    () -> assertThat(result.getNotifications()).isNotNull(),
                    // Profile 검증
                    () -> assertThat(result.getProfile().getUserId()).isEqualTo(5L),
                    () -> assertThat(result.getProfile().getNickname()).isEqualTo("감성작가"),
                    () -> assertThat(result.getProfile().getEmail()).isEqualTo("artist@example.com"),
                    () -> assertThat(result.getProfile().getProfileImageUrl()).isNotNull(),
                    // Stats 검증
                    () -> assertThat(result.getStats().getFollowerCount()).isEqualTo(1250),
                    () -> assertThat(result.getStats().getProductCount()).isEqualTo(28),
                    () -> assertThat(result.getStats().getTodaysSales()).isEqualTo(125000),
                    () -> assertThat(result.getStats().getTodaysOrders()).isEqualTo(8),
                    () -> assertThat(result.getStats().getTotalSales()).isEqualTo(2450000),
                    () -> assertThat(result.getStats().getTotalOrders()).isEqualTo(156),
                    () -> assertThat(result.getStats().getAverageRating()).isEqualTo(4.8),
                    () -> assertThat(result.getStats().getPendingOrders()).isEqualTo(3),
                    // Trends 검증
                    () -> assertThat(result.getTrends().getMeta()).isNotNull(),
                    () -> assertThat(result.getTrends().getSeries()).isNotNull(),
                    () -> assertThat(result.getTrends().getChanges()).isNotNull(),
                    // Trends Meta 검증
                    () -> assertThat(result.getTrends().getMeta().getRange()).isEqualTo("6M"),
                    () -> assertThat(result.getTrends().getMeta().getFrom()).isEqualTo("2025-03-01"),
                    () -> assertThat(result.getTrends().getMeta().getTo()).isEqualTo("2025-09-01"),
                    () -> assertThat(result.getTrends().getMeta().getInterval()).isEqualTo("WEEK"),
                    () -> assertThat(result.getTrends().getMeta().getTimezone()).isEqualTo("Asia/Seoul"),
                    () -> assertThat(result.getTrends().getMeta().getMaxPoints()).isEqualTo(400),
                    () -> assertThat(result.getTrends().getMeta().getCompare()).isNotNull(),
                    // Series 검증
                    () -> assertThat(result.getTrends().getSeries().getSales()).isNotNull(),
                    () -> assertThat(result.getTrends().getSeries().getOrders()).isNotNull(),
                    () -> assertThat(result.getTrends().getSeries().getFollowers()).isNotNull(),
                    () -> assertThat(result.getTrends().getSeries().getSales().getUnit()).isEqualTo("KRW"),
                    () -> assertThat(result.getTrends().getSeries().getSales().getTotal()).isEqualTo(2450000),
                    () -> assertThat(result.getTrends().getSeries().getSales().getPoints()).hasSize(7),
                    () -> assertThat(result.getTrends().getSeries().getOrders().getUnit()).isEqualTo("COUNT"),
                    () -> assertThat(result.getTrends().getSeries().getOrders().getTotal()).isEqualTo(156),
                    () -> assertThat(result.getTrends().getSeries().getFollowers().getUnit()).isEqualTo("COUNT"),
                    () -> assertThat(result.getTrends().getSeries().getFollowers().getTotal()).isEqualTo(1250),
                    // Changes 검증
                    () -> assertThat(result.getTrends().getChanges().getSales().getDelta()).isEqualTo(-40000),
                    () -> assertThat(result.getTrends().getChanges().getSales().getRate()).isEqualTo(-0.242),
                    () -> assertThat(result.getTrends().getChanges().getOrders().getDelta()).isEqualTo(1),
                    () -> assertThat(result.getTrends().getChanges().getOrders().getRate()).isEqualTo(0.143),
                    () -> assertThat(result.getTrends().getChanges().getFollowers().getDelta()).isEqualTo(70),
                    () -> assertThat(result.getTrends().getChanges().getFollowers().getRate()).isEqualTo(0.059),
                    // Notifications 검증
                    () -> assertThat(result.getNotifications().getOrderAlerts()).hasSize(2),
                    () -> assertThat(result.getNotifications().getFundingAlerts()).hasSize(1),
                    () -> assertThat(result.getNotifications().getOrderAlerts().get(0).getType()).isEqualTo("NEW_ORDER"),
                    () -> assertThat(result.getNotifications().getOrderAlerts().get(0).getMessage()).isEqualTo("새로운 주문 3건이 접수되었습니다"),
                    () -> assertThat(result.getNotifications().getOrderAlerts().get(0).getCount()).isEqualTo(3),
                    () -> assertThat(result.getNotifications().getFundingAlerts().get(0).getType()).isEqualTo("FUNDING_GOAL_ACHIEVED"),
                    // 기타 정보 검증
                    () -> assertThat(result.getServerTime()).isEqualTo(LocalDateTime.of(2025, 12, 24, 15, 0)),
                    () -> assertThat(result.getTimezone()).isEqualTo("Asia/Seoul")
            );
        }

        @Test
        @DisplayName("메인 현황 조회 - 커스텀 기간")
        void getMainStats_Success_CustomRange() {
            // Given
            String range = "ALL", from = "2025-01-01", to = "2025-12-31", interval = "MONTH", tz = "Asia/Seoul";

            // When
            ArtistMainResponse result = artistDashboardService.getMainStats(
                    TEST_AUTHORIZATION, range, from, to, interval, tz);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getProfile()).isNotNull(),
                    () -> assertThat(result.getStats()).isNotNull(),
                    () -> assertThat(result.getTrends()).isNotNull(),
                    () -> assertThat(result.getNotifications()).isNotNull(),
                    () -> assertThat(result.getServerTime()).isNotNull(),
                    () -> assertThat(result.getTimezone()).isEqualTo("Asia/Seoul")
            );
        }
    }
}
