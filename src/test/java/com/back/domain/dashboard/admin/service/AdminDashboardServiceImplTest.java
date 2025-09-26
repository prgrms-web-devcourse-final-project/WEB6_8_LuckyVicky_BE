package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminUserResponse;
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

    @Test
    @DisplayName("상품 목록 조회 - 완전한 응답 구조 반환")
    void getProducts_ReturnsCompleteStructure() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then - 핵심 구조 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).isNotNull(),
                () -> assertThat(result.page()).isNotNegative(),
                () -> assertThat(result.size()).isPositive(),
                () -> assertThat(result.totalElements()).isNotNegative(),
                () -> assertThat(result.totalPages()).isNotNegative()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 요약 정보 검증")
    void getProducts_ValidatesSummary() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then - 요약 정보 검증
        AdminProductResponse.Summary summary = result.summary();
        assertAll(
                () -> assertThat(summary.totalProducts()).isEqualTo(2340),
                () -> assertThat(summary.sellingProducts()).isEqualTo(2105),
                () -> assertThat(summary.stoppedProducts()).isEqualTo(235),

                // 비즈니스 규칙 검증 - 전체 = 판매중 + 판매중지
                () -> assertThat(summary.totalProducts())
                        .isEqualTo(summary.sellingProducts() + summary.stoppedProducts())
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 상품 데이터 구조 검증")
    void getProducts_ValidatesProductStructure() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then - 상품 데이터 구조 검증
        assertAll(
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.content()).hasSize(2)
        );

        AdminProductResponse.Product firstProduct = result.content().get(0);
        assertAll(
                // 기본 정보 검증
                () -> assertThat(firstProduct.productId()).isEqualTo(123357L),
                () -> assertThat(firstProduct.productNumber()).isEqualTo("0123357"),
                () -> assertThat(firstProduct.name()).isNotBlank(),
                () -> assertThat(firstProduct.sellingStatus()).isIn("SELLING", "STOPPED"),
                () -> assertThat(firstProduct.registeredAt()).isNotNull(),

                // 작가 정보 검증
                () -> assertThat(firstProduct.artist()).isNotNull(),
                () -> assertThat(firstProduct.artist().id()).isPositive(),
                () -> assertThat(firstProduct.artist().name()).isNotBlank(),

                // 카테고리 정보 검증
                () -> assertThat(firstProduct.category()).isNotNull(),
                () -> assertThat(firstProduct.category().id()).isPositive(),
                () -> assertThat(firstProduct.category().name()).isNotBlank(),

                // 권한 정보 검증
                () -> assertThat(firstProduct.permissions()).isNotNull(),
                () -> assertThat(firstProduct.permissions().moderate()).isNotNull(),
                () -> assertThat(firstProduct.permissions().delete()).isNotNull(),
                () -> assertThat(firstProduct.permissions().statusChange()).isNotNull(),

                // 모더레이션 정보 검증
                () -> assertThat(firstProduct.moderation()).isNotNull(),
                () -> assertThat(firstProduct.moderation().hasPendingRequest()).isNotNull()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - metrics=false 시 메트릭 null 검증")
    void getProducts_MetricsFalse_ReturnsNullMetrics() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then - metrics=false일 때 null 검증
        AdminProductResponse.Product firstProduct = result.content().get(0);
        assertAll(
                () -> assertThat(firstProduct.averageRating()).isNull(),
                () -> assertThat(firstProduct.reviewCount()).isNull(),
                () -> assertThat(firstProduct.revenue()).isNull()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - metrics=true 시 메트릭 포함 검증")
    void getProducts_MetricsTrue_IncludesMetrics() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", true);

        // Then - metrics=true일 때 값 검증
        AdminProductResponse.Product firstProduct = result.content().get(0);
        assertAll(
                () -> assertThat(firstProduct.averageRating()).isNotNull(),
                () -> assertThat(firstProduct.averageRating()).isGreaterThanOrEqualTo(0.0),
                () -> assertThat(firstProduct.averageRating()).isLessThanOrEqualTo(5.0),
                () -> assertThat(firstProduct.reviewCount()).isNotNull(),
                () -> assertThat(firstProduct.reviewCount()).isNotNegative(),
                () -> assertThat(firstProduct.revenue()).isNotNull(),
                () -> assertThat(firstProduct.revenue()).isNotNegative()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 페이지네이션 로직 검증")
    void getProducts_ValidatesPagination() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then - 페이지네이션 비즈니스 로직 검증
        assertAll(
                () -> assertThat(result.page()).isEqualTo(0),
                () -> assertThat(result.size()).isEqualTo(20),
                () -> assertThat(result.totalPages())
                        .isEqualTo((int) Math.ceil((double) result.totalElements() / result.size())),

                // 첫 페이지 검증
                () -> assertThat(result.hasPrevious()).isFalse(),
                () -> assertThat(result.hasNext()).isTrue()
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 완전한 응답 구조 반환")
    void getUsers_ReturnsCompleteStructure() {
        // When
        AdminUserResponse result = adminDashboardService.getUsers(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, "joinedAt", "DESC");

        // Then - 핵심 구조 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).isNotNull(),
                () -> assertThat(result.page()).isNotNegative(),
                () -> assertThat(result.size()).isPositive(),
                () -> assertThat(result.totalElements()).isNotNegative(),
                () -> assertThat(result.totalPages()).isNotNegative()
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 요약 정보 검증")
    void getUsers_ValidatesSummary() {
        // When
        AdminUserResponse result = adminDashboardService.getUsers(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, "joinedAt", "DESC");

        // Then - 요약 정보 검증
        AdminUserResponse.Summary summary = result.summary();
        assertAll(
                () -> assertThat(summary.totalUsers()).isEqualTo(13240),
                () -> assertThat(summary.activeUsers()).isEqualTo(12810),
                () -> assertThat(summary.suspendedUsers()).isEqualTo(280),
                () -> assertThat(summary.blacklistedUsers()).isEqualTo(150),
                () -> assertThat(summary.artistUsers()).isEqualTo(1000),

                // 비즈니스 규칙 검증 - 전체 = 활동중 + 정지 + 블랙리스트
                () -> assertThat(summary.totalUsers())
                        .isEqualTo(summary.activeUsers() + summary.suspendedUsers() + summary.blacklistedUsers())
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 사용자 데이터 구조 검증")
    void getUsers_ValidatesUserStructure() {
        // When
        AdminUserResponse result = adminDashboardService.getUsers(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, "joinedAt", "DESC");

        // Then - 사용자 데이터 구조 검증
        assertAll(
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.content()).hasSize(5)
        );

        // 일반 USER 검증
        AdminUserResponse.User regularUser = result.content().get(0);
        assertAll(
                () -> assertThat(regularUser.userId()).isPositive(),
                () -> assertThat(regularUser.role()).isEqualTo("USER"),
                () -> assertThat(regularUser.artist().id()).isNull(),
                () -> assertThat(regularUser.artist().name()).isNull(),
                () -> assertThat(regularUser.permissions().canBlacklist()).isTrue(),
                () -> assertThat(regularUser.permissions().canUnblacklist()).isFalse()
        );

        // ARTIST 검증
        AdminUserResponse.User artistUser = result.content().stream()
                .filter(u -> "ARTIST".equals(u.role()))
                .findFirst()
                .orElseThrow();
        assertAll(
                () -> assertThat(artistUser.artist().id()).isNotNull(),
                () -> assertThat(artistUser.artist().name()).isNotBlank()
        );

        // BLACKLISTED 검증
        AdminUserResponse.User blacklistedUser = result.content().stream()
                .filter(u -> "BLACKLISTED".equals(u.accountStatus()))
                .findFirst()
                .orElseThrow();
        assertAll(
                () -> assertThat(blacklistedUser.permissions().canBlacklist()).isFalse(),
                () -> assertThat(blacklistedUser.permissions().canUnblacklist()).isTrue()
        );
    }
}
