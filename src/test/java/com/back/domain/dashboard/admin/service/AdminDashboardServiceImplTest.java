package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * AdminDashboardServiceImpl 테스트
 * 비즈니스 로직과 데이터 일관성에 집중
 * 2025.09.26 신규 생성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 대시보드 서비스 구현체 테스트")
class AdminDashboardServiceImplTest {

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-admin-token";
    private static final String TEST_ADMIN_ROLE = "SUPER_ADMIN";

    @Test
    @DisplayName("전체 현황 조회 - 완전한 응답 구조 반환")
    void getOverview_ReturnsCompleteStructure() {
        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, "1M", "DAY", "MONTH", "Asia/Seoul");

        // Then - 핵심 구조와 비즈니스 로직 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.overview()).isNotNull(),
                () -> assertThat(result.charts()).isNotNull(),
                () -> assertThat(result.alerts()).isNotNull(),
                () -> assertThat(result.serverTime()).isNotNull(),
                () -> assertThat(result.timezone()).isEqualTo("Asia/Seoul")
        );
    }

    @Test
    @DisplayName("전체 현황 통계 - 비즈니스 규칙 검증")
    void getOverview_ValidatesOverviewStatistics() {
        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, "1M", "DAY", "MONTH", "Asia/Seoul");

        // Then - 전체 현황 통계 검증
        AdminOverviewResponse.Overview overview = result.overview();
        assertAll(
                // 기본 통계 검증
                () -> assertThat(overview.userCount().count()).isEqualTo(12450L),
                () -> assertThat(overview.userCount().label()).isEqualTo("가입자 수"),
                () -> assertThat(overview.userCount().unit()).isEqualTo("명"),
                () -> assertThat(overview.orderStats().count()).isEqualTo(8945L),
                () -> assertThat(overview.salesStats().count()).isEqualTo(145780000L),

                // 비즈니스 규칙 검증 - 모든 수치가 음수가 아님
                () -> assertThat(overview.userCount().count()).isNotNegative(),
                () -> assertThat(overview.orderStats().count()).isNotNegative(),
                () -> assertThat(overview.salesStats().count()).isNotNegative()
        );
    }

    @Test
    @DisplayName("차트 데이터 - 구조와 일관성 검증")
    void getOverview_ValidatesChartData() {
        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, "1M", "DAY", "MONTH", "Asia/Seoul");

        // Then - 차트 데이터 검증
        AdminOverviewResponse.Charts charts = result.charts();
        assertAll(
                // 메타 정보 검증
                () -> assertThat(charts.meta().range()).isEqualTo("1M"),
                () -> assertThat(charts.meta().granularity()).isEqualTo("DAY"),
                () -> assertThat(charts.meta().timezone()).isEqualTo("Asia/Seoul"),

                // 매출 트렌드 검증 (간소화된 데이터)
                () -> assertThat(charts.salesTrend().series().sales()).hasSize(2),
                () -> assertThat(charts.salesTrend().series().orders()).hasSize(2),
                () -> assertThat(charts.salesTrend().delta().sales().delta()).isEqualTo(480000L),

                // 사용자 증가 현황 검증 (간소화된 데이터)
                () -> assertThat(charts.userGrowth().series().users()).hasSize(2),
                () -> assertThat(charts.userGrowth().series().artists()).hasSize(2),

                // 카테고리 분포 검증
                () -> assertThat(charts.categoryDistribution().totalProducts()).isEqualTo(2340),
                () -> assertThat(charts.categoryDistribution().buckets()).hasSize(5)
        );
    }

    @Test
    @DisplayName("승인 대기 알림 - 데이터 일관성 검증")
    void getOverview_ValidatesAlerts() {
        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, "1M", "DAY", "MONTH", "Asia/Seoul");

        // Then - 알림 데이터 검증
        AdminOverviewResponse.Alerts alerts = result.alerts();
        assertAll(
                () -> assertThat(alerts.artistApprovalPending()).hasSize(2),
                () -> assertThat(alerts.artistApprovalPending().getFirst().artistId()).isEqualTo(1001L),
                () -> assertThat(alerts.fundingApprovalPending()).hasSize(1),
                () -> assertThat(alerts.fundingApprovalPending().getFirst().fundingId()).isEqualTo(456789L)
        );
    }
}
