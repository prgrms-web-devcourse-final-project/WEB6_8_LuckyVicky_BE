package com.back.domain.dashboard.admin.controller;

import com.back.domain.dashboard.admin.dto.request.AdminOverviewRequest;
import com.back.domain.dashboard.admin.dto.request.AdminProductSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminSettlementRequest;
import com.back.domain.dashboard.admin.dto.request.AdminUserSearchRequest;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminSettlementResponse;
import com.back.domain.dashboard.admin.dto.response.AdminUserResponse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * AdminDashboardController 테스트
 * 컨트롤러 레이어의 HTTP 응답과 서비스 호출에 집중
 * 2025.09.26 생성
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
        assertThat(data).isNotNull();
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 성공")
    void getProducts_Success() {
        // Given
        AdminProductResponse mockResponse = createMockProductResponse();
        given(adminDashboardService.getProducts(
                anyString(), anyString(), anyInt(), anyInt(), 
                any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
                .willReturn(mockResponse);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, null, null, null, null, null, null, null, null, false);

        // When
        ResponseEntity<RsData<AdminProductResponse>> response =
                adminDashboardController.getProducts(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        AdminProductResponse data = assertSuccessResponse(response, "관리자 상품 목록 조회 성공");
        assertThat(data).isNotNull();
    }

    @Test
    @DisplayName("관리자 사용자 목록 조회 성공")
    void getUsers_Success() {
        // Given
        AdminUserResponse mockResponse = createMockUserResponse();
        given(adminDashboardService.getUsers(
                anyString(), anyString(), anyInt(), anyInt(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(mockResponse);

        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null, null, null);

        // When
        ResponseEntity<RsData<AdminUserResponse>> response =
                adminDashboardController.getUsers(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        AdminUserResponse data = assertSuccessResponse(response, "관리자 사용자 목록 조회 성공");
        assertThat(data).isNotNull();
    }

    @Test
    @DisplayName("관리자 매출/정산 조회 성공")
    void getSettlements_Success() {
        // Given
        AdminSettlementResponse mockResponse = createMockSettlementResponse();
        given(adminDashboardService.getSettlements(
                anyString(), anyString(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        AdminSettlementRequest request = new AdminSettlementRequest(
                2025, null, "MONTH", "Asia/Seoul");

        // When
        ResponseEntity<RsData<AdminSettlementResponse>> response =
                adminDashboardController.getSettlements(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        AdminSettlementResponse data = assertSuccessResponse(response, "관리자 매출/정산 조회 성공");
        assertThat(data).isNotNull();
    }

    // Mock 데이터 생성 헬퍼 메서드들
    private AdminOverviewResponse createMockOverviewResponse() {
        AdminOverviewResponse.Overview overview = new AdminOverviewResponse.Overview(
                new AdminOverviewResponse.StatInfo(12450L, "가입자 수", "명", 234L, 0.019),
                new AdminOverviewResponse.StatInfo(8945L, "주문", "건", 156L, 0.018),
                new AdminOverviewResponse.StatInfo(145780000L, "매출", "원", 19560000L, 0.155),
                new AdminOverviewResponse.StatInfo(2340L, "상품수", "개", 45L, 0.02),
                new AdminOverviewResponse.StatInfo(45L, "펀딩수", "개", 8L, 0.216),
                new AdminOverviewResponse.StatInfo(156L, "작가수", "명", 12L, 0.083)
        );

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
                        "2025-12-24", 2340,
                        List.of(new AdminOverviewResponse.CategoryBucket(1L, "스티커", 820, 0.35))
                )
        );

        AdminOverviewResponse.Alerts alerts = new AdminOverviewResponse.Alerts(
                List.of(new AdminOverviewResponse.ArtistApproval(1001L, "작가A",
                        LocalDateTime.of(2025, 12, 23, 9, 10))),
                List.of(new AdminOverviewResponse.FundingApproval(456789L, "한정 제품",
                        LocalDateTime.of(2025, 12, 23, 10, 15)))
        );

        return new AdminOverviewResponse(overview, charts, alerts, LocalDateTime.now(), "Asia/Seoul");
    }

    private AdminProductResponse createMockProductResponse() {
        AdminProductResponse.Summary summary = new AdminProductResponse.Summary(100, 80, 20);
        List<AdminProductResponse.Product> products = List.of(
                new AdminProductResponse.Product(
                        1L, "P001", "테스트 상품",
                        new AdminProductResponse.Artist(1L, "테스트 작가"),
                        "SELLING",
                        new AdminProductResponse.Category(1L, "테스트 카테고리"),
                        LocalDateTime.now().toLocalDate(),
                        new AdminProductResponse.Permissions(true, true, true),
                        null, null, null,
                        new AdminProductResponse.Moderation(false, null)
                )
        );
        return new AdminProductResponse(summary, products, 0, 20, 100, 5, true, false);
    }

    private AdminUserResponse createMockUserResponse() {
        AdminUserResponse.Summary summary = new AdminUserResponse.Summary(100, 90, 5, 5, 10);
        List<AdminUserResponse.User> users = List.of(
                new AdminUserResponse.User(
                        1L, "testuser", "테스트 사용자", "USER",
                        new AdminUserResponse.Artist(null, null),
                        new AdminUserResponse.Grade("SEED", "새싹"),
                        "ACTIVE", LocalDateTime.now().toLocalDate(), LocalDateTime.now(),
                        new AdminUserResponse.Permissions(true, false)
                )
        );
        return new AdminUserResponse(summary, users, 0, 20, 100, 5, true, false);
    }

    private AdminSettlementResponse createMockSettlementResponse() {
        AdminSettlementResponse.Scope scope = new AdminSettlementResponse.Scope(2025, null);
        AdminSettlementResponse.Summary summary = new AdminSettlementResponse.Summary(
                10000000L, 1000000L, 9000000L);

        List<AdminSettlementResponse.DataPoint> grossSales = List.of(
                new AdminSettlementResponse.DataPoint("2025-01-01", 1000000L));
        AdminSettlementResponse.Chart chart = new AdminSettlementResponse.Chart(
                new AdminSettlementResponse.Series(grossSales, grossSales, grossSales));

        List<AdminSettlementResponse.TableRow> table = List.of(
                new AdminSettlementResponse.TableRow("2025-01-01", 1000000L, 100000L, 900000L));

        return new AdminSettlementResponse(scope, "MONTH", "Asia/Seoul", summary, chart, table, LocalDateTime.now());
    }
}
