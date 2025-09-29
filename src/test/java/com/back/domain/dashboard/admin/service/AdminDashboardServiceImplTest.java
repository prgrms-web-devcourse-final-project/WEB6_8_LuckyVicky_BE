package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationDetailResponse;
import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationResponse;
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
 * 핵심 비즈니스 로직과 데이터 일관성에 집중
 * 2025.09.28 수정
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 대시보드 서비스 구현체 테스트")
class AdminDashboardServiceImplTest {

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-admin-token";
    private static final String TEST_ADMIN_ROLE = "SUPER_ADMIN";

    @Test
    @DisplayName("전체 현황 조회 - 핵심 비즈니스 규칙 검증")
    void getOverview_ValidatesEssentialBusinessRules() {
        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, "1M", "DAY", "MONTH", "Asia/Seoul");

        // Then - 핵심 비즈니스 규칙만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.timezone()).isEqualTo("Asia/Seoul"),
                // 음수 방지 규칙
                () -> assertThat(result.overview().userCount().count()).isNotNegative(),
                () -> assertThat(result.overview().orderStats().count()).isNotNegative(),
                () -> assertThat(result.overview().salesStats().count()).isNotNegative()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 핵심 비즈니스 규칙 검증")
    void getProducts_ValidatesEssentialBusinessRules() {
        // When
        AdminProductResponse result = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);

        // Then - 핵심 비즈니스 규칙만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                // 전체 = 판매중 + 판매중지
                () -> assertThat(result.summary().totalProducts())
                        .isEqualTo(result.summary().sellingProducts() + result.summary().stoppedProducts())
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - metrics 옵션 동작 검증")
    void getProducts_ValidatesMetricsOption() {
        // When
        AdminProductResponse withoutMetrics = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", false);
        
        AdminProductResponse withMetrics = adminDashboardService.getProducts(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, "registeredAt", "DESC", true);

        // Then - metrics 옵션 동작 검증
        assertAll(
                // metrics=false일 때 null
                () -> assertThat(withoutMetrics.content().get(0).averageRating()).isNull(),
                () -> assertThat(withoutMetrics.content().get(0).reviewCount()).isNull(),
                () -> assertThat(withoutMetrics.content().get(0).revenue()).isNull(),
                
                // metrics=true일 때 값 존재
                () -> assertThat(withMetrics.content().get(0).averageRating()).isNotNull(),
                () -> assertThat(withMetrics.content().get(0).reviewCount()).isNotNull(),
                () -> assertThat(withMetrics.content().get(0).revenue()).isNotNull()
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 핵심 비즈니스 규칙 검증")
    void getUsers_ValidatesEssentialBusinessRules() {
        // When
        AdminUserResponse result = adminDashboardService.getUsers(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, "joinedAt", "DESC");

        // Then - 핵심 비즈니스 규칙만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                // 전체 = 활동중 + 정지 + 블랙리스트
                () -> assertThat(result.summary().totalUsers())
                        .isEqualTo(result.summary().activeUsers() + 
                                  result.summary().suspendedUsers() + 
                                  result.summary().blacklistedUsers())
        );
    }

    @Test
    @DisplayName("매출/정산 조회 - 핵심 비즈니스 규칙 검증")
    void getSettlements_ValidatesEssentialBusinessRules() {
        // When
        AdminSettlementResponse result = adminDashboardService.getSettlements(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 2025, null, "MONTH", "Asia/Seoul");

        // Then - 핵심 비즈니스 규칙만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.table()).hasSize(12), // 연도별 12개월
                // 순수익 = 매출 - 정산금
                () -> result.table().forEach(row -> 
                        assertThat(row.netIncome()).isEqualTo(row.grossSales() - row.artistPayout()))
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
    @DisplayName("펀딩 목록 조회 - 핵심 비즈니스 규칙 검증")
    void getFundings_ValidatesEssentialBusinessRules() {
        // When
        AdminFundingResponse result = adminDashboardService.getFundings(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, null, null, null, null, null, null, "endDate", "ASC");

        // Then - 핵심 비즈니스 규칙만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                // 전체 = 진행중 + 일시정지 + 완료 + 취소
                () -> assertThat(result.summary().totalFundings())
                        .isEqualTo(result.summary().activeFundings() + 
                                  result.summary().pausedFundings() + 
                                  result.summary().completedFundings() + 
                                  result.summary().cancelledFundings()),
                // 달성률 계산 검증
                () -> result.content().forEach(funding -> 
                        assertThat(funding.achievementRate())
                                .isEqualTo((int) ((funding.currentAmount() * 100) / funding.targetAmount())))
        );
    }

    @Test
    @DisplayName("입점 신청 목록 조회 - 핵심 비즈니스 규칙 검증")
    void getArtistApplications_ValidatesEssentialBusinessRules() {
        // When
        AdminArtistApplicationResponse result = adminDashboardService.getArtistApplications(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 0, 20,
                null, null, null, null, "submittedAt", "DESC");

        // Then - 핵심 비즈니스 규칙만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                // 전체 = 대기 + 승인 + 거절
                () -> assertThat(result.summary().totalApplications())
                        .isEqualTo(result.summary().pending() + 
                                  result.summary().approved() + 
                                  result.summary().rejected())
        );
    }

    @Test
    @DisplayName("입점 신청 상세 조회 - 필수 정보 검증")
    void getArtistApplicationDetail_ValidatesEssentialInfo() {
        // When
        AdminArtistApplicationDetailResponse result = adminDashboardService.getArtistApplicationDetail(
                TEST_AUTHORIZATION, TEST_ADMIN_ROLE, 80123L);

        // Then - 필수 정보만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.applicationId()).isEqualTo(80123L),
                () -> assertThat(result.status()).isEqualTo("PENDING"),
                () -> assertThat(result.artist().userId()).isPositive(),
                () -> assertThat(result.contact().email()).contains("@"),
                () -> assertThat(result.business().registrationNo()).isNotBlank()
        );
    }
}
