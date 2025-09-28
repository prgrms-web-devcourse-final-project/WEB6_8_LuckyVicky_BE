package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminFundingResponse;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminSettlementResponse;
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
 * 2025.09.26 생성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 대시보드 서비스 구현체 테스트")
class AdminDashboardServiceImplTest {

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-admin-token";
    private static final String TEST_ADMIN_ROLE = "SUPER_ADMIN";

    @Test
    @DisplayName("전체 현황 조회 - 응답 구조 검증")
    void getOverview_ReturnsCompleteStructure() {
        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, "1M", "DAY", "MONTH", "Asia/Seoul");

        // Then - 핵심 구조 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.overview()).isNotNull(),
                () -> assertThat(result.charts()).isNotNull(),
                () -> assertThat(result.alerts()).isNotNull(),
                () -> assertThat(result.serverTime()).isNotNull(),
                () -> assertThat(result.timezone()).isEqualTo("Asia/Seoul"),

                // 비즈니스 규칙 - 음수 방지
                () -> assertThat(result.overview().userCount().count()).isNotNegative(),
                () -> assertThat(result.overview().orderStats().count()).isNotNegative(),
                () -> assertThat(result.overview().salesStats().count()).isNotNegative()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 응답 구조 및 비즈니스 규칙 검증")
    void getProducts_ValidatesStructureAndBusinessRules() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then
        assertAll(
                // 구조 검증
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),

                // 페이지네이션 검증
                () -> assertThat(result.page()).isNotNegative(),
                () -> assertThat(result.size()).isPositive(),
                () -> assertThat(result.totalElements()).isNotNegative(),
                () -> assertThat(result.totalPages()).isNotNegative(),

                // 비즈니스 규칙 - 전체 = 판매중 + 판매중지
                () -> assertThat(result.summary().totalProducts())
                        .isEqualTo(result.summary().sellingProducts() + result.summary().stoppedProducts())
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - metrics 옵션에 따른 동작 검증")
    void getProducts_ValidatesMetricsOption() {
        // When - metrics=false
        AdminProductResponse resultWithoutMetrics = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // When - metrics=true  
        AdminProductResponse resultWithMetrics = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", true);

        // Then
        AdminProductResponse.Product productWithoutMetrics = resultWithoutMetrics.content().get(0);
        AdminProductResponse.Product productWithMetrics = resultWithMetrics.content().get(0);

        assertAll(
                // metrics=false일 때 null
                () -> assertThat(productWithoutMetrics.averageRating()).isNull(),
                () -> assertThat(productWithoutMetrics.reviewCount()).isNull(),
                () -> assertThat(productWithoutMetrics.revenue()).isNull(),

                // metrics=true일 때 값 존재 및 유효성 검증
                () -> assertThat(productWithMetrics.averageRating()).isNotNull(),
                () -> assertThat(productWithMetrics.averageRating()).isBetween(0.0, 5.0),
                () -> assertThat(productWithMetrics.reviewCount()).isNotNegative(),
                () -> assertThat(productWithMetrics.revenue()).isNotNegative()
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 응답 구조 및 비즈니스 규칙 검증")
    void getUsers_ValidatesStructureAndBusinessRules() {
        // When
        AdminUserResponse result = adminDashboardService.getUsers(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, "joinedAt", "DESC");

        // Then
        assertAll(
                // 구조 검증
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),

                // 비즈니스 규칙 - 전체 = 활동중 + 정지 + 블랙리스트
                () -> assertThat(result.summary().totalUsers())
                        .isEqualTo(result.summary().activeUsers() + 
                                  result.summary().suspendedUsers() + 
                                  result.summary().blacklistedUsers())
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 역할별 데이터 일관성 검증")
    void getUsers_ValidatesRoleConsistency() {
        // When
        AdminUserResponse result = adminDashboardService.getUsers(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, "joinedAt", "DESC");

        // Then - 역할별 데이터 일관성 검증
        result.content().forEach(user -> {
            if ("ARTIST".equals(user.role())) {
                // ARTIST는 작가 정보가 있어야 함
                assertAll(
                        () -> assertThat(user.artist().id()).isNotNull(),
                        () -> assertThat(user.artist().name()).isNotBlank()
                );
            } else if ("USER".equals(user.role())) {
                // 일반 USER는 작가 정보가 null이어야 함
                assertAll(
                        () -> assertThat(user.artist().id()).isNull(),
                        () -> assertThat(user.artist().name()).isNull()
                );
            }

            // 블랙리스트 사용자는 권한이 반대여야 함
            if ("BLACKLISTED".equals(user.accountStatus())) {
                assertAll(
                        () -> assertThat(user.permissions().canBlacklist()).isFalse(),
                        () -> assertThat(user.permissions().canUnblacklist()).isTrue()
                );
            }
        });
    }

    @Test
    @DisplayName("매출/정산 조회 - 응답 구조 및 비즈니스 규칙 검증")
    void getSettlements_ValidatesStructureAndBusinessRules() {
        // When
        AdminSettlementResponse result = adminDashboardService.getSettlements(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 2025, null, "MONTH", "Asia/Seoul");

        // Then
        assertAll(
                // 구조 검증
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.scope()).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.chart()).isNotNull(),
                () -> assertThat(result.table()).isNotNull()
        );

        // 비즈니스 규칙 검증 - 순수익 = 매출 - 정산금
        result.table().forEach(row -> {
            assertAll(
                    () -> assertThat(row.netIncome())
                            .isEqualTo(row.grossSales() - row.artistPayout()),
                    () -> assertThat(row.grossSales()).isNotNegative(),
                    () -> assertThat(row.artistPayout()).isNotNegative(),
                    () -> assertThat(row.netIncome()).isNotNegative()
            );
        });
    }

    @Test
    @DisplayName("매출/정산 조회 - 집계 단위별 데이터 크기 검증")
    void getSettlements_ValidatesDataSizeByGranularity() {
        // When - 연도별 월간 집계
        AdminSettlementResponse yearlyResult = adminDashboardService.getSettlements(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 2025, null, "MONTH", "Asia/Seoul");

        // When - 특정 월 일별 집계 (9월 = 30일)
        AdminSettlementResponse monthlyResult = adminDashboardService.getSettlements(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 2025, 9, "DAY", "Asia/Seoul");

        // Then
        assertAll(
                // 연도별 집계는 12개월
                () -> assertThat(yearlyResult.table()).hasSize(12),
                () -> assertThat(yearlyResult.chart().series().grossSales()).hasSize(12),

                // 9월 일별 집계는 30일
                () -> assertThat(monthlyResult.table()).hasSize(30),
                () -> assertThat(monthlyResult.chart().series().grossSales()).hasSize(30)
        );
    }

    @Test
    @DisplayName("매출/정산 조회 - 기본값 연도 처리 검증")
    void getSettlements_HandlesDefaultYear() {
        // When
        AdminSettlementResponse result = adminDashboardService.getSettlements(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, null, null, "MONTH", "Asia/Seoul");

        // Then
        assertThat(result.scope().year()).isEqualTo(java.time.Year.now().getValue());
    }

    @Test
    @DisplayName("펀딩 목록 조회 - 응답 구조 및 비즈니스 규칙 검증")
    void getFundings_ValidatesStructureAndBusinessRules() {
        // When
        AdminFundingResponse result = adminDashboardService.getFundings(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, null, null, null, "endDate", "ASC");

        // Then
        assertAll(
                // 구조 검증
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),

                // 페이지네이션 검증
                () -> assertThat(result.page()).isNotNegative(),
                () -> assertThat(result.size()).isPositive(),
                () -> assertThat(result.totalElements()).isNotNegative(),
                () -> assertThat(result.totalPages()).isNotNegative(),

                // 비즈니스 규칙 - 전체 = 진행중 + 일시정지 + 완료 + 취소
                () -> assertThat(result.summary().totalFundings())
                        .isEqualTo(result.summary().activeFundings() + 
                                  result.summary().pausedFundings() + 
                                  result.summary().completedFundings() + 
                                  result.summary().cancelledFundings())
        );
    }

    @Test
    @DisplayName("펀딩 목록 조회 - 달성률 및 플래그 일관성 검증")
    void getFundings_ValidatesAchievementAndFlags() {
        // When
        AdminFundingResponse result = adminDashboardService.getFundings(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, null, null, null, "endDate", "ASC");

        // Then - 달성률과 플래그 일관성 검증
        result.content().forEach(funding -> {
            assertAll(
                    // 달성률 계산 검증
                    () -> assertThat(funding.achievementRate())
                            .isEqualTo((int) ((funding.currentAmount() * 100) / funding.targetAmount())),
                    
                    // 목표 달성 플래그 일관성
                    () -> assertThat(funding.flags().goalAchieved())
                            .isEqualTo(funding.achievementRate() >= 100),
                    
                    // 금액 및 카운트 음수 방지
                    () -> assertThat(funding.targetAmount()).isPositive(),
                    () -> assertThat(funding.currentAmount()).isNotNegative(),
                    () -> assertThat(funding.supporterCount()).isNotNegative(),
                    () -> assertThat(funding.remainingDays()).isNotNegative()
            );
        });
    }
}
