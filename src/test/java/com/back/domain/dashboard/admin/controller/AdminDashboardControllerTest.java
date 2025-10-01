package com.back.domain.dashboard.admin.controller;

import com.back.domain.dashboard.admin.dto.request.AdminArtistApplicationSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminFundingSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminOverviewRequest;
import com.back.domain.dashboard.admin.dto.request.AdminProductSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminSettlementRequest;
import com.back.domain.dashboard.admin.dto.request.AdminUserSearchRequest;
import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationDetailResponse;
import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationResponse;
import com.back.domain.dashboard.admin.dto.response.AdminFundingResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * AdminDashboardController 테스트
 * HTTP 응답과 서비스 호출 검증에 집중
 * 2025.09.28 수정
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
     * 성공 응답 검증 헬퍼 메서드
     */
    private <T> T assertSuccessResponse(ResponseEntity<RsData<T>> response, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<T> body = response.getBody();
        assertThat(body.resultCode()).isEqualTo(SUCCESS_CODE);
        assertThat(body.msg()).isEqualTo(expectedMessage);
        assertThat(body.data()).isNotNull();
        
        return body.data();
    }

    @Test
    @DisplayName("관리자 대시보드 전체 현황 조회 성공")
    void getOverview_Success() {
        // Given
        AdminOverviewResponse mockResponse = mock(AdminOverviewResponse.class);
        given(adminDashboardService.getOverview(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(mockResponse);

        AdminOverviewRequest request = new AdminOverviewRequest("1M", "DAY", "MONTH", "Asia/Seoul");

        // When
        ResponseEntity<RsData<AdminOverviewResponse>> response =
                adminDashboardController.getOverview(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        assertSuccessResponse(response, "관리자 메인 현황 조회 성공");
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 성공")
    void getProducts_Success() {
        // Given
        AdminProductResponse mockResponse = mock(AdminProductResponse.class);
        given(adminDashboardService.getProducts(anyString(), anyString(), anyInt(), anyInt(), 
                any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
                .willReturn(mockResponse);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, null, null, null, null, null, null, null, null, false);

        // When
        ResponseEntity<RsData<AdminProductResponse>> response =
                adminDashboardController.getProducts(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        assertSuccessResponse(response, "관리자 상품 목록 조회 성공");
    }

    @Test
    @DisplayName("관리자 사용자 목록 조회 성공")
    void getUsers_Success() {
        // Given
        AdminUserResponse mockResponse = mock(AdminUserResponse.class);
        given(adminDashboardService.getUsers(anyString(), anyString(), anyInt(), anyInt(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(mockResponse);

        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null, null, null);

        // When
        ResponseEntity<RsData<AdminUserResponse>> response =
                adminDashboardController.getUsers(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        assertSuccessResponse(response, "관리자 사용자 목록 조회 성공");
    }

    @Test
    @DisplayName("관리자 매출/정산 조회 성공")
    void getSettlements_Success() {
        // Given
        AdminSettlementResponse mockResponse = mock(AdminSettlementResponse.class);
        given(adminDashboardService.getSettlements(anyString(), anyString(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        AdminSettlementRequest request = new AdminSettlementRequest(2025, null, "MONTH", "Asia/Seoul");

        // When
        ResponseEntity<RsData<AdminSettlementResponse>> response =
                adminDashboardController.getSettlements(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        assertSuccessResponse(response, "관리자 매출/정산 조회 성공");
    }

    @Test
    @DisplayName("관리자 펀딩 목록 조회 성공")
    void getFundings_Success() {
        // Given
        AdminFundingResponse mockResponse = mock(AdminFundingResponse.class);
        given(adminDashboardService.getFundings(anyString(), anyString(), anyInt(), anyInt(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(mockResponse);

        AdminFundingSearchRequest request = new AdminFundingSearchRequest(
                0, 20, null, null, null, null, null, null, null, null, null, null, null, null);

        // When
        ResponseEntity<RsData<AdminFundingResponse>> response =
                adminDashboardController.getFundings(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        assertSuccessResponse(response, "관리자 펀딩 모니터링 조회 성공");
    }

    @Test
    @DisplayName("관리자 입점 신청 목록 조회 성공")
    void getArtistApplications_Success() {
        // Given
        AdminArtistApplicationResponse mockResponse = mock(AdminArtistApplicationResponse.class);
        given(adminDashboardService.getArtistApplications(anyString(), anyString(), anyInt(), anyInt(),
                any(), any(), any(), any(), any(), any()))
                .willReturn(mockResponse);

        AdminArtistApplicationSearchRequest request = new AdminArtistApplicationSearchRequest(
                0, 20, null, null, null, null, null, null);

        // When
        ResponseEntity<RsData<AdminArtistApplicationResponse>> response =
                adminDashboardController.getArtistApplications(BEARER_TOKEN, ADMIN_ROLE, request);

        // Then
        assertSuccessResponse(response, "입점 신청 목록 조회 성공");
    }

    @Test
    @DisplayName("관리자 입점 신청 상세 조회 성공")
    void getArtistApplicationDetail_Success() {
        // Given
        AdminArtistApplicationDetailResponse mockResponse = mock(AdminArtistApplicationDetailResponse.class);
        given(adminDashboardService.getArtistApplicationDetail(anyString(), anyString(), anyLong()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<AdminArtistApplicationDetailResponse>> response =
                adminDashboardController.getArtistApplicationDetail(BEARER_TOKEN, ADMIN_ROLE, 80123L);

        // Then
        assertSuccessResponse(response, "입점 신청 상세 조회 성공");
    }
}
