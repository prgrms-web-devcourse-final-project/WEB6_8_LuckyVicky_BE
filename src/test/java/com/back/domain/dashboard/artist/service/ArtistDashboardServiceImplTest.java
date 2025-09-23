package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.sevice.ArtistDashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

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
                // 프로필 기본 정보
                () -> assertThat(result.getProfile().getUserId()).isEqualTo(5L),
                () -> assertThat(result.getProfile().getNickname()).isEqualTo("감성작가"),
                // 통계 데이터 일관성
                () -> assertThat(result.getStats().getTotalSales()).isNotNegative(),
                () -> assertThat(result.getStats().getTodaysSales()).isLessThanOrEqualTo(result.getStats().getTotalSales()),
                () -> assertThat(result.getStats().getAverageRating()).isBetween(0.0, 5.0),
                // 트렌드 데이터 구조
                () -> assertThat(result.getTrends().getSeries().getSales().getPoints()).isNotEmpty(),
                () -> assertThat(result.getTrends().getChanges().getSales().getDelta()).isNotNull(),
                // 알림 데이터
                () -> assertThat(result.getNotifications().getOrderAlerts()).hasSize(2),
                () -> assertThat(result.getNotifications().getFundingAlerts()).hasSize(1),
                // 메타 정보
                () -> assertThat(result.getServerTime()).isEqualTo(LocalDateTime.of(2025, 12, 24, 15, 0)),
                () -> assertThat(result.getTimezone()).isEqualTo("Asia/Seoul")
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
                () -> assertThat(result.getContent()).hasSize(3),
                // 페이징 일관성
                () -> assertThat(result.getPage()).isNotNegative(),
                () -> assertThat(result.getSize()).isPositive(),
                () -> assertThat(result.getTotalElements()).isEqualTo(28),
                () -> assertThat(result.getTotalPages()).isEqualTo(3),
                () -> assertThat(result.isHasNext()).isTrue(),
                () -> assertThat(result.isHasPrevious()).isFalse(),
                // 상품 데이터 검증
                () -> assertThat(result.getContent().get(0).getProductNumber()).isNotBlank(),
                () -> assertThat(result.getContent().get(0).getPrice()).isNotNegative(),
                () -> assertThat(result.getContent().get(0).getSellingStatus()).isEqualTo("SELLING")
        );
    }

    @Test
    @DisplayName("지갑 잔액 조회 - 금액 일관성과 비즈니스 규칙 검증")
    void getCashBalance_ReturnsConsistentBalance() {
        // When
        ArtistCashResponse.Balance result = artistDashboardService.getCashBalance(TEST_AUTHORIZATION);

        // Then - 비즈니스 규칙 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                // 기본 정보
                () -> assertThat(result.getCurrentBalance()).isEqualTo(72000),
                () -> assertThat(result.getPendingSettlement()).isEqualTo(15000),
                () -> assertThat(result.getPendingWithdrawal()).isEqualTo(0),
                () -> assertThat(result.getWithdrawable()).isEqualTo(72000),
                () -> assertThat(result.getCurrency()).isEqualTo("KRW"),
                // 비즈니스 규칙 - 환전 가능 금액은 현재 잔액과 같거나 작아야 함
                () -> assertThat(result.getWithdrawable()).isLessThanOrEqualTo(result.getCurrentBalance()),
                // 금액들은 모두 음수가 아니어야 함
                () -> assertThat(result.getCurrentBalance()).isNotNegative(),
                () -> assertThat(result.getPendingSettlement()).isNotNegative(),
                () -> assertThat(result.getPendingWithdrawal()).isNotNegative(),
                // 업데이트 시간
                () -> assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 9, 24, 10, 0))
        );
    }

    @Test
    @DisplayName("다양한 파라미터로 메인 통계 조회")
    void getMainStats_WithDifferentParameters() {
        // When
        ArtistMainResponse result = artistDashboardService.getMainStats(
                TEST_AUTHORIZATION, "ALL", "2025-01-01", "2025-12-31", "MONTH", "Asia/Seoul");

        // Then - 파라미터와 무관하게 일관된 구조 반환
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProfile()).isNotNull(),
                () -> assertThat(result.getStats()).isNotNull(),
                () -> assertThat(result.getTrends()).isNotNull(),
                () -> assertThat(result.getNotifications()).isNotNull(),
                () -> assertThat(result.getTimezone()).isEqualTo("Asia/Seoul")
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 거래 데이터와 통계 일관성 검증")
    void getCashHistory_ReturnsConsistentTransactionData() {
        // When
        ArtistCashHistoryResponse.List result = artistDashboardService.getCashHistory(
                TEST_AUTHORIZATION, 0, 20, null, null, null, null, "transactedAt", "DESC");

        // Then - 거래 데이터와 통계 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                // 요약 정보 검증
                () -> assertThat(result.getSummary().getPeriodDepositTotal()).isEqualTo(74000),
                () -> assertThat(result.getSummary().getPeriodWithdrawalTotal()).isEqualTo(64000),
                () -> assertThat(result.getSummary().getPeriodNet()).isEqualTo(10000),
                // 비즈니스 규칙 - 순 증감은 입금 - 환전
                () -> assertThat(result.getSummary().getPeriodNet()).isEqualTo(
                        result.getSummary().getPeriodDepositTotal() - result.getSummary().getPeriodWithdrawalTotal()),
                // 거래 내역 검증
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent().get(0).getTxId()).isNotBlank(),
                () -> assertThat(result.getContent().get(0).getTransactedAt()).isNotBlank(),
                () -> assertThat(result.getContent().get(0).getBalanceAfter()).isNotNegative(),
                // 페이징 정보 검증
                () -> assertThat(result.getPage()).isNotNegative(),
                () -> assertThat(result.getSize()).isPositive(),
                () -> assertThat(result.getTotalElements()).isEqualTo(3),
                () -> assertThat(result.getTotalPages()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 거래 유형별 필터링")
    void getCashHistory_WithTypeFilter() {
        // When - DEPOSIT 타입만 조회
        ArtistCashHistoryResponse.List result = artistDashboardService.getCashHistory(
                TEST_AUTHORIZATION, 0, 20, "DEPOSIT", null, null, null, "transactedAt", "DESC");

        // Then - 필터링 결과 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                // 거래 데이터 일관성 (입금 거래 포함되어 있는지 확인)
                () -> assertThat(result.getContent().stream()
                        .anyMatch(tx -> "DEPOSIT".equals(tx.getType()))).isTrue(),
                () -> assertThat(result.getContent().stream()
                        .filter(tx -> "DEPOSIT".equals(tx.getType()))
                        .allMatch(tx -> tx.getDepositAmount() > 0 && tx.getWithdrawalAmount() == 0)).isTrue()
        );
    }
}
