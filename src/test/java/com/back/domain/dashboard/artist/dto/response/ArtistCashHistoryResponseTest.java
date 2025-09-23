package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistCashHistoryResponse DTO 테스트
 * 핵심 비즈니스 로직에 집중
 * 2025.09.24 생성
 */
@DisplayName("ArtistCashHistoryResponse DTO 테스트")
public class ArtistCashHistoryResponseTest {

    @Test
    @DisplayName("캐시 내역 구조 생성 및 검증")
    void createCashHistoryStructure_Success() {
        // When
        ArtistCashHistoryResponse.List cashHistory = createSampleCashHistory();

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(cashHistory).isNotNull(),
                () -> assertThat(cashHistory.getSummary()).isNotNull(),
                () -> assertThat(cashHistory.getContent()).isNotNull(),
                () -> assertThat(cashHistory.getPage()).isNotNegative(),
                () -> assertThat(cashHistory.getSize()).isPositive(),
                () -> assertThat(cashHistory.getTotalElements()).isNotNegative()
        );
    }

    @Test
    @DisplayName("거래 내역 비즈니스 로직 검증")
    void validateBusinessLogic_Success() {
        // When
        ArtistCashHistoryResponse.List cashHistory = createSampleCashHistory();
        ArtistCashHistoryResponse.Summary summary = cashHistory.getSummary();

        // Then - 핵심 비즈니스 규칙 검증
        assertAll(
                // 통계 일관성: 순 증감 = 입금 - 환전
                () -> assertThat(summary.getPeriodNet()).isEqualTo(
                        summary.getPeriodDepositTotal() - summary.getPeriodWithdrawalTotal()),
                // 금액 검증
                () -> assertThat(summary.getPeriodDepositTotal()).isNotNegative(),
                () -> assertThat(summary.getPeriodWithdrawalTotal()).isNotNegative(),
                // 거래 내역 기본 검증
                () -> assertThat(cashHistory.getContent()).isNotEmpty(),
                () -> assertThat(cashHistory.getContent().get(0).getTxId()).isNotBlank(),
                () -> assertThat(cashHistory.getContent().get(0).getBalanceAfter()).isNotNegative()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistCashHistoryResponse.List response = ArtistCashHistoryResponse.List.builder()
                .summary(ArtistCashHistoryResponse.Summary.builder()
                        .periodDepositTotal(74000)
                        .periodWithdrawalTotal(64000)
                        .periodNet(10000)
                        .build())
                .content(Arrays.asList(
                        ArtistCashHistoryResponse.Transaction.builder()
                                .txId("TX-20250924-0001")
                                .type("DEPOSIT")
                                .depositAmount(10000)
                                .withdrawalAmount(0)
                                .balanceAfter(10000)
                                .status("COMPLETED")
                                .build()
                ))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.getSummary().getPeriodDepositTotal()).isEqualTo(74000),
                () -> assertThat(response.getSummary().getPeriodNet()).isEqualTo(10000),
                () -> assertThat(response.getContent().get(0).getTxId()).isEqualTo("TX-20250924-0001"),
                () -> assertThat(response.getContent().get(0).getType()).isEqualTo("DEPOSIT"),
                () -> assertThat(response.getTotalElements()).isEqualTo(1)
        );
    }

    // -----------헬퍼 메서드 -------------

    private ArtistCashHistoryResponse.List createSampleCashHistory() {
        return ArtistCashHistoryResponse.List.builder()
                .summary(ArtistCashHistoryResponse.Summary.builder()
                        .periodDepositTotal(74000)
                        .periodWithdrawalTotal(64000)
                        .periodNet(10000)
                        .build())
                .content(Arrays.asList(
                        ArtistCashHistoryResponse.Transaction.builder()
                                .txId("TX-20250924-0001")
                                .transactedAt("2025-09-24T09:12:00+09:00")
                                .type("DEPOSIT")
                                .depositAmount(10000)
                                .withdrawalAmount(0)
                                .balanceAfter(10000)
                                .status("COMPLETED")
                                .build()
                ))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
