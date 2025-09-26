package com.back.domain.dashboard.admin.controller;

import com.back.domain.dashboard.admin.dto.request.AdminOverviewRequest;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.service.AdminDashboardService;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * AdminDashboardController 테스트
 * 2025.09.26 신규 생성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 대시보드 컨트롤러 테스트")
class AdminDashboardControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    private static final String BEARER_TOKEN = "Bearer test-admin-token-123";
    private static final String ADMIN_ROLE = "SUPER_ADMIN";
    private static final String SUCCESS_CODE = "200";

    /**
     * 성공 응답 검증 및 데이터 추출 헬퍼 메서드
     */
    private <T> T assertSuccessResponse(ResponseEntity<RsData<T>> response, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<T> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.resultCode()).isEqualTo(SUCCESS_CODE);
        assertThat(body.msg()).isEqualTo(expectedMessage);

        T data = body.data();
        assertThat(data).isNotNull();
        return data;
    }

    /**
     * Mock 응답 데이터 생성 헬퍼 메서드
     */
    private AdminOverviewResponse createMockOverviewResponse() {
        // 전체 현황 통계
        AdminOverviewResponse.Overview overview = new AdminOverviewResponse.Overview(
                new AdminOverviewResponse.StatInfo(12450L, "가입자 수", "명", 234L, 0.019),
                new AdminOverviewResponse.StatInfo(8945L, "주문", "건", 156L, 0.018),
                new AdminOverviewResponse.StatInfo(145780000L, "매출", "원", 19560000L, 0.155),
                new AdminOverviewResponse.StatInfo(2340L, "상품수", "개", 45L, 0.02),
                new AdminOverviewResponse.StatInfo(45L, "펀딩수", "개", 8L, 0.216),
                new AdminOverviewResponse.StatInfo(156L, "작가수", "명", 12L, 0.083)
        );

        // 차트 데이터
        AdminOverviewResponse.Charts charts = new AdminOverviewResponse.Charts(
                new AdminOverviewResponse.ChartMeta("1M", "DAY", "Asia/Seoul"),
                new AdminOverviewResponse.SalesTrend(
                        new AdminOverviewResponse.SalesSeries(
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-24", 2340000L)),
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-24", 234L))
                        ),
                        new AdminOverviewResponse.SalesDelta(
                                new AdminOverviewResponse.DeltaInfo(480000L, 0.23),
                                new AdminOverviewResponse.DeltaInfo(18L, 0.084)
                        )
                ),
                new AdminOverviewResponse.UserGrowth(
                        new AdminOverviewResponse.UserSeries(
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-01", 12450L)),
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-01", 156L))
                        ),
                        new AdminOverviewResponse.UserDelta(
                                new AdminOverviewResponse.DeltaInfo(450L, 0.037),
                                new AdminOverviewResponse.DeltaInfo(36L, 0.30)
                        )
                ),
                new AdminOverviewResponse.CategoryDistribution(
                        "2025-12-24",
                        2340,
                        List.of(
                                new AdminOverviewResponse.CategoryBucket(1L, "스티커", 820, 0.35),
                                new AdminOverviewResponse.CategoryBucket(2L, "다이어리", 420, 0.179)
                        )
                )
        );

        // 알림
        AdminOverviewResponse.Alerts alerts = new AdminOverviewResponse.Alerts(
                List.of(new AdminOverviewResponse.ArtistApproval(1001L, "작가A", 
                        LocalDateTime.of(2025, 12, 23, 9, 10))),
                List.of(new AdminOverviewResponse.FundingApproval(456789L, "한정 제품", 
                        LocalDateTime.of(2025, 12, 23, 10, 15)))
        );

        return new AdminOverviewResponse(
                overview, charts, alerts, LocalDateTime.now(), "Asia/Seoul"
        );
    }

    @Test
    @DisplayName("관리자 대시보드 전체 현황 조회 성공")
    void getOverview_Success() {
        // Given
        AdminOverviewResponse mockResponse = createMockOverviewResponse();
        given(adminDashboardService.getOverview(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(mockResponse);

        AdminOverviewRequest request = new AdminOverviewRequest(
                "1M", "DAY", "MONTH", "Asia/Seoul");

        // When
        ResponseEntity<RsData<AdminOverviewResponse>> response =
                adminDashboardController.getOverview(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        AdminOverviewResponse data = assertSuccessResponse(response, "관리자 메인 현황 조회 성공");

        assertAll(
                // 기본 구조 검증
                () -> assertThat(data.overview()).isNotNull(),
                () -> assertThat(data.charts()).isNotNull(),
                () -> assertThat(data.alerts()).isNotNull(),
                () -> assertThat(data.serverTime()).isNotNull(),
                () -> assertThat(data.timezone()).isEqualTo("Asia/Seoul"),

                // 전체 현황 통계 검증
                () -> assertThat(data.overview().userCount().count()).isEqualTo(12450L),
                () -> assertThat(data.overview().userCount().label()).isEqualTo("가입자 수"),
                () -> assertThat(data.overview().userCount().unit()).isEqualTo("명"),
                () -> assertThat(data.overview().userCount().delta()).isEqualTo(234L),
                () -> assertThat(data.overview().userCount().rate()).isEqualTo(0.019),

                () -> assertThat(data.overview().salesStats().count()).isEqualTo(145780000L),
                () -> assertThat(data.overview().salesStats().label()).isEqualTo("매출"),
                () -> assertThat(data.overview().salesStats().unit()).isEqualTo("원"),

                () -> assertThat(data.overview().artistCount().count()).isEqualTo(156L),
                () -> assertThat(data.overview().fundingCount().count()).isEqualTo(45L),

                // 차트 데이터 검증
                () -> assertThat(data.charts().meta().range()).isEqualTo("1M"),
                () -> assertThat(data.charts().meta().granularity()).isEqualTo("DAY"),
                () -> assertThat(data.charts().meta().timezone()).isEqualTo("Asia/Seoul"),

                // 매출 트렌드 검증
                () -> assertThat(data.charts().salesTrend().series().sales()).hasSize(1),
                () -> assertThat(data.charts().salesTrend().series().orders()).hasSize(1),
                () -> assertThat(data.charts().salesTrend().delta().sales().delta()).isEqualTo(480000L),
                () -> assertThat(data.charts().salesTrend().delta().orders().delta()).isEqualTo(18L),

                // 사용자 증가 현황 검증
                () -> assertThat(data.charts().userGrowth().series().users()).hasSize(1),
                () -> assertThat(data.charts().userGrowth().series().artists()).hasSize(1),
                () -> assertThat(data.charts().userGrowth().delta().users().delta()).isEqualTo(450L),
                () -> assertThat(data.charts().userGrowth().delta().artists().rate()).isEqualTo(0.30),

                // 카테고리 분포 검증
                () -> assertThat(data.charts().categoryDistribution().asOf()).isEqualTo("2025-12-24"),
                () -> assertThat(data.charts().categoryDistribution().totalProducts()).isEqualTo(2340),
                () -> assertThat(data.charts().categoryDistribution().buckets()).hasSize(2),
                () -> assertThat(data.charts().categoryDistribution().buckets().getFirst().name()).isEqualTo("스티커"),
                () -> assertThat(data.charts().categoryDistribution().buckets().getFirst().count()).isEqualTo(820),
                () -> assertThat(data.charts().categoryDistribution().buckets().getFirst().share()).isEqualTo(0.35),

                // 알림 검증
                () -> assertThat(data.alerts().artistApprovalPending()).hasSize(1),
                () -> assertThat(data.alerts().fundingApprovalPending()).hasSize(1),
                () -> assertThat(data.alerts().artistApprovalPending().getFirst().artistId()).isEqualTo(1001L),
                () -> assertThat(data.alerts().artistApprovalPending().getFirst().nickname()).isEqualTo("작가A"),
                () -> assertThat(data.alerts().fundingApprovalPending().getFirst().fundingId()).isEqualTo(456789L),
                () -> assertThat(data.alerts().fundingApprovalPending().getFirst().productName()).isEqualTo("한정 제품")
        );
    }

    @Test
    @DisplayName("관리자 대시보드 전체 현황 조회 - 기본값 파라미터")
    void getOverview_WithDefaultParameters() {
        // Given
        AdminOverviewResponse mockResponse = createMockOverviewResponse();
        given(adminDashboardService.getOverview(
                eq(BEARER_TOKEN), eq(ADMIN_ROLE), eq("1M"), eq("DAY"), eq("MONTH"), eq("Asia/Seoul")))
                .willReturn(mockResponse);

        AdminOverviewRequest request = new AdminOverviewRequest(
                null, null, null, null); // 모든 파라미터 null로 기본값 테스트

        // When
        ResponseEntity<RsData<AdminOverviewResponse>> response =
                adminDashboardController.getOverview(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        AdminOverviewResponse data = assertSuccessResponse(response, "관리자 메인 현황 조회 성공");
        assertAll(
                () -> assertThat(data).isNotNull(),
                () -> assertThat(data.charts().meta().range()).isEqualTo("1M"),
                () -> assertThat(data.charts().meta().granularity()).isEqualTo("DAY"),
                () -> assertThat(data.timezone()).isEqualTo("Asia/Seoul")
        );
    }


}
