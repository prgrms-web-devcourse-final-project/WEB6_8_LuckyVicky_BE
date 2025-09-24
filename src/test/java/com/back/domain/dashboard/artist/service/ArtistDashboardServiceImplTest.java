package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistDashboardServiceImpl 테스트
 * 비즈니스 로직과 데이터 일관성에 집중
 * 2025.09.23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("작가 대시보드 서비스 구현체 테스트")
class ArtistDashboardServiceImplTest {

    @InjectMocks
    private ArtistDashboardServiceImpl artistDashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Test
    @DisplayName("메인 통계 조회 - 완전한 응답 구조 반환")
    void getMainStats_ReturnsCompleteStructure() {
        // When
        ArtistMainResponse result = artistDashboardService.getMainStats(
                TEST_AUTHORIZATION, "6M", null, null, "AUTO", "Asia/Seoul");

        // Then - 핵심 구조와 비즈니스 로직 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProfile()).isNotNull(),
                () -> assertThat(result.getStats()).isNotNull(),
                () -> assertThat(result.getTrends()).isNotNull(),
                () -> assertThat(result.getNotifications()).isNotNull(),
                // 비즈니스 규칙 검증
                () -> assertThat(result.getStats().getTotalSales()).isNotNegative(),
                () -> assertThat(result.getStats().getAverageRating()).isBetween(0.0, 5.0),
                () -> assertThat(result.getTrends().getSeries().getSales().getPoints()).isNotEmpty(),
                () -> assertThat(result.getServerTime()).isNotNull()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 페이징과 데이터 일관성 검증")
    void getProducts_ReturnsPaginatedResults() {
        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(
                TEST_AUTHORIZATION, 0, 10, null, null, "registrationDate", "DESC");

        // Then - 페이징 로직과 데이터 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(1),
                () -> assertThat(result.isHasNext()).isFalse(),
                () -> assertThat(result.isHasPrevious()).isFalse(),
                () -> assertThat(result.getContent().get(0).getPrice()).isNotNegative(),
                () -> assertThat(result.getContent().get(0).getSellingStatus()).isEqualTo("SELLING")
        );
    }

    @Test
    @DisplayName("지갑 잔액 조회 - 비즈니스 규칙 검증")
    void getCashBalance_ReturnsConsistentBalance() {
        // When
        ArtistCashResponse.Balance result = artistDashboardService.getCashBalance(TEST_AUTHORIZATION);

        // Then - 비즈니스 규칙 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCurrentBalance()).isEqualTo(72000),
                () -> assertThat(result.getCurrency()).isEqualTo("KRW"),
                // 핵심 비즈니스 규칙 - 환전 가능 금액은 현재 잔액 이하
                () -> assertThat(result.getWithdrawable()).isLessThanOrEqualTo(result.getCurrentBalance()),
                () -> assertThat(result.getCurrentBalance()).isNotNegative(),
                () -> assertThat(result.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 거래 데이터 일관성 검증")
    void getCashHistory_ReturnsConsistentTransactionData() {
        // When
        ArtistCashHistoryResponse.List result = artistDashboardService.getCashHistory(
                TEST_AUTHORIZATION, 0, 20, null, null, null, null, "transactedAt", "DESC");

        // Then - 거래 데이터와 통계 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(2),
                // 핵심 비즈니스 규칙 - 순 증감은 입금 - 환전
                () -> assertThat(result.getSummary().getPeriodNet()).isEqualTo(
                        result.getSummary().getPeriodDepositTotal() - result.getSummary().getPeriodWithdrawalTotal()),
                () -> assertThat(result.getContent().get(0).getBalanceAfter()).isNotNegative(),
                () -> assertThat(result.getTotalElements()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("주문 내역 조회 - 통계와 목록 일관성 검증")
    void getOrders_ReturnsConsistentOrderData() {
        // When
        ArtistOrderResponse.List result = artistDashboardService.getOrders(
                TEST_AUTHORIZATION, 0, 20, null, null, null, null, "orderDate", "DESC");

        // Then - 주문 통계와 목록 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getSummary().getTotal()).isEqualTo(156),
                () -> assertThat(result.getSummary().getPending()).isNotNegative(),
                () -> assertThat(result.getContent().get(0).getTotalAmount()).isPositive(),
                () -> assertThat(result.getTotalElements()).isEqualTo(156)
        );
    }

    @Test
    @DisplayName("취소 요청 목록 조회 - 통계와 목록 일관성 검증")
    void getCancellationRequests_ReturnsConsistentData() {
        // When
        ArtistCancellationResponse.List result = artistDashboardService.getCancellationRequests(
                TEST_AUTHORIZATION, 0, 20, null, null, null, null, null, "requestDate", "DESC");

        // Then - 취소 요청 통계와 목록 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getBulkActions()).hasSize(2),
                // 핵심 비즈니스 규칙 - 상태별 합계가 전체와 일치
                () -> assertThat(result.getSummary().getPending() + result.getSummary().getApproved()
                        + result.getSummary().getRejected()).isEqualTo(result.getSummary().getTotal()),
                () -> assertThat(result.getContent().get(0).getRefundAmount()).isPositive(),
                () -> assertThat(result.getTotalElements()).isEqualTo(8)
        );
    }

    @Test
    @DisplayName("교환 요청 목록 조회 - 통계와 목록 일관성 검증")
    void getExchangeRequests_ReturnsConsistentData() {
        // When
        ArtistExchangeResponse.List result = artistDashboardService.getExchangeRequests(
                TEST_AUTHORIZATION, 0, 20, null, null, null, null, null, "requestDate", "DESC");

        // Then - 교환 요청 통계와 목록 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getBulkActions()).hasSize(2),
                // 핵심 비즈니스 규칙 - 상태별 합계가 전체와 일치
                () -> assertThat(result.getSummary().getPending() + result.getSummary().getApproved()
                        + result.getSummary().getRejected()).isEqualTo(result.getSummary().getTotal()),
                () -> assertThat(result.getContent().get(0).getOrderItem().getPrice()).isPositive(),
                () -> assertThat(result.getTotalElements()).isEqualTo(5)
        );
    }

    @Test
    @DisplayName("작가 설정 정보 조회 - 완전한 설정 정보 반환")
    void getSettings_ReturnsCompleteSettings() {
        // When
        ArtistSettingsResponse result = artistDashboardService.getSettings(TEST_AUTHORIZATION);

        // Then - 설정 정보 구조와 데이터 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProfile()).isNotNull(),
                () -> assertThat(result.getBusiness()).isNotNull(),
                () -> assertThat(result.getPayout()).isNotNull(),
                () -> assertThat(result.getPermissions()).isNotNull(),
                // 프로필 정보 검증
                () -> assertThat(result.getProfile().getNickname()).isEqualTo("작가명입니다"),
                () -> assertThat(result.getProfile().getBio()).isNotEmpty(),
                () -> assertThat(result.getProfile().getSns()).hasSize(1),
                () -> assertThat(result.getProfile().getSns().get(0).getPlatform()).isEqualTo("Instagram"),
                () -> assertThat(result.getProfile().getProfileImageUrl()).isNotEmpty(),
                // 사업자 정보 검증
                () -> assertThat(result.getBusiness().getAddress()).isNotEmpty(),
                () -> assertThat(result.getBusiness().getBusinessRegistrationNo()).isEqualTo("123-45-67890"),
                () -> assertThat(result.getBusiness().getTelemarketingReportNo()).isNotEmpty(),
                () -> assertThat(result.getBusiness().isVerified()).isTrue(),
                // 정산 계좌 정보 검증
                () -> assertThat(result.getPayout().getBankCode()).isEqualTo("088"),
                () -> assertThat(result.getPayout().getBankName()).isEqualTo("신한"),
                () -> assertThat(result.getPayout().getAccountHolder()).isEqualTo("홍길동"),
                () -> assertThat(result.getPayout().getAccountMasked()).contains("****"),
                () -> assertThat(result.getPayout().getStatus()).isEqualTo("VERIFIED"),
                // 권한 정보 검증
                () -> assertThat(result.getPermissions().isCanEditProfile()).isTrue(),
                () -> assertThat(result.getPermissions().isCanEditBusiness()).isTrue(),
                () -> assertThat(result.getPermissions().isCanEditPayout()).isTrue()
        );
    }
}