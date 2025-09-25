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
 * 2025.09.25 수정
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
                () -> assertThat(result.profile()).isNotNull(),
                () -> assertThat(result.stats()).isNotNull(),
                () -> assertThat(result.trends()).isNotNull(),
                () -> assertThat(result.notifications()).isNotNull(),
                // 비즈니스 규칙 검증
                () -> assertThat(result.stats().totalSales()).isNotNegative(),
                () -> assertThat(result.stats().averageRating()).isBetween(0.0, 5.0),
                () -> assertThat(result.trends().series().sales().points()).isNotEmpty(),
                () -> assertThat(result.serverTime()).isNotNull()
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
                () -> assertThat(result.getContent().getFirst().price()).isNotNegative(),
                () -> assertThat(result.getContent().getFirst().sellingStatus()).isEqualTo("SELLING")
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
                () -> assertThat(result.currentBalance()).isEqualTo(72000),
                () -> assertThat(result.currency()).isEqualTo("KRW"),
                // 핵심 비즈니스 규칙 - 환전 가능 금액은 현재 잔액 이하
                () -> assertThat(result.withdrawable()).isLessThanOrEqualTo(result.currentBalance()),
                () -> assertThat(result.currentBalance()).isNotNegative(),
                () -> assertThat(result.updatedAt()).isNotNull()
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
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).hasSize(2),
                // 핵심 비즈니스 규칙 - 순 증감은 입금 - 환전
                () -> assertThat(result.summary().periodNet()).isEqualTo(
                        result.summary().periodDepositTotal() - result.summary().periodWithdrawalTotal()),
                () -> assertThat(result.content().getFirst().balanceAfter()).isNotNegative(),
                () -> assertThat(result.totalElements()).isEqualTo(2)
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
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.summary().total()).isEqualTo(156),
                () -> assertThat(result.summary().pending()).isNotNegative(),
                () -> assertThat(result.content().getFirst().totalAmount()).isPositive(),
                () -> assertThat(result.totalElements()).isEqualTo(156)
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
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.bulkActions()).hasSize(2),
                // 핵심 비즈니스 규칙 - 상태별 합계가 전체와 일치
                () -> assertThat(result.summary().pending() + result.summary().approved()
                        + result.summary().rejected()).isEqualTo(result.summary().total()),
                () -> assertThat(result.content().getFirst().refundAmount()).isPositive(),
                () -> assertThat(result.totalElements()).isEqualTo(8)
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
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.bulkActions()).hasSize(2),
                // 핵심 비즈니스 규칙 - 상태별 합계가 전체와 일치
                () -> assertThat(result.summary().pending() + result.summary().approved()
                        + result.summary().rejected()).isEqualTo(result.summary().total()),
                () -> assertThat(result.content().getFirst().orderItem().price()).isPositive(),
                () -> assertThat(result.totalElements()).isEqualTo(5)
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
                () -> assertThat(result.profile()).isNotNull(),
                () -> assertThat(result.business()).isNotNull(),
                () -> assertThat(result.payout()).isNotNull(),
                () -> assertThat(result.permissions()).isNotNull(),
                // 프로필 정보 검증
                () -> assertThat(result.profile().nickname()).isEqualTo("작가명입니다"),
                () -> assertThat(result.profile().bio()).isNotEmpty(),
                () -> assertThat(result.profile().sns()).hasSize(1),
                () -> assertThat(result.profile().sns().getFirst().platform()).isEqualTo("Instagram"),
                () -> assertThat(result.profile().profileImageUrl()).isNotEmpty(),
                // 사업자 정보 검증
                () -> assertThat(result.business().address()).isNotEmpty(),
                () -> assertThat(result.business().businessRegistrationNo()).isEqualTo("123-45-67890"),
                () -> assertThat(result.business().telemarketingReportNo()).isNotEmpty(),
                () -> assertThat(result.business().verified()).isTrue(),
                // 정산 계좌 정보 검증
                () -> assertThat(result.payout().bankCode()).isEqualTo("088"),
                () -> assertThat(result.payout().bankName()).isEqualTo("신한"),
                () -> assertThat(result.payout().accountHolder()).isEqualTo("홍길동"),
                () -> assertThat(result.payout().accountMasked()).contains("****"),
                () -> assertThat(result.payout().status()).isEqualTo("VERIFIED"),
                // 권한 정보 검증
                () -> assertThat(result.permissions().canEditProfile()).isTrue(),
                () -> assertThat(result.permissions().canEditBusiness()).isTrue(),
                () -> assertThat(result.permissions().canEditPayout()).isTrue()
        );
    }

    @Test
    @DisplayName("작가 펀딩 목록 조회 - 완전한 응답 구조 반환")
    void getFundings_ReturnsCompleteStructure() {
        // When
        ArtistFundingResponse.List result = artistDashboardService.getFundings(
                TEST_AUTHORIZATION, 0, 20, null, null, null, null, null, null, null, "endDate", "ASC");

        // Then - 구조와 비즈니스 로직 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getBulkActions()).isNotEmpty(),
                // 요약 정보 검증
                () -> assertThat(result.getSummary().totalFundings()).isEqualTo(15),
                () -> assertThat(result.getSummary().activeFundings()).isEqualTo(8),
                () -> assertThat(result.getSummary().completedFundings()).isEqualTo(6),
                () -> assertThat(result.getSummary().cancelledFundings()).isEqualTo(1),
                // 비즈니스 규칙 검증 - 달성률과 목표달성 플래그 일관성
                () -> assertThat(result.getContent().getFirst().achievementRate()).isEqualTo(100),
                () -> assertThat(result.getContent().getFirst().flags().goalAchieved()).isTrue(),
                () -> assertThat(result.getContent().get(1).achievementRate()).isEqualTo(1500),
                () -> assertThat(result.getContent().get(1).flags().goalAchieved()).isTrue(),
                // 권한 로직 검증
                () -> assertThat(result.getContent().getFirst().permissions().canRequestSale()).isTrue(),
                () -> assertThat(result.getContent().get(1).permissions().canRequestSale()).isTrue(),
                // 페이징 검증
                () -> assertThat(result.getTotalElements()).isEqualTo(15),
                () -> assertThat(result.getTotalPages()).isEqualTo(1),
                () -> assertThat(result.isHasNext()).isFalse()
        );
    }
}