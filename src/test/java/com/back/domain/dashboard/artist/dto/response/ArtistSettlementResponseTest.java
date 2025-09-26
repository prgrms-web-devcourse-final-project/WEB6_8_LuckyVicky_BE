package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistSettlementResponse DTO 테스트
 * 전체 구조와 핵심 비즈니스 로직에 집중
 * 2025.09.25 생성
 */
@DisplayName("ArtistSettlementResponse DTO 테스트")
public class ArtistSettlementResponseTest {

    @Test
    @DisplayName("전체 응답 구조 생성 및 검증")
    void createCompleteResponse_Success() {
        // Given
        LocalDateTime serverTime = LocalDateTime.now();
        
        // When
        ArtistSettlementResponse response = createSampleResponse(serverTime);

        // Then - 핵심 구조만 검증
        assertAll(
                () -> assertThat(response.scope()).isNotNull(),
                () -> assertThat(response.granularity()).isEqualTo("MONTH"),
                () -> assertThat(response.timezone()).isEqualTo("Asia/Seoul"),
                () -> assertThat(response.summary()).isNotNull(),
                () -> assertThat(response.chart()).isNotNull(),
                () -> assertThat(response.table()).isNotNull(),
                () -> assertThat(response.serverTime()).isEqualTo(serverTime)
        );
    }

    @Test
    @DisplayName("정산 요약 정보 구조 검증")
    void validateSummaryStructure_Success() {
        // When
        ArtistSettlementResponse response = createSampleResponse(LocalDateTime.now());
        ArtistSettlementResponse.Summary summary = response.summary();

        // Then - 요약 정보 구조 검증
        assertAll(
                () -> assertThat(summary.totalSales()).isNotNull(),
                () -> assertThat(summary.totalCommission()).isNotNull(),
                () -> assertThat(summary.totalNetIncome()).isNotNull(),
                () -> assertThat(summary.totalSales().label()).isEqualTo("총 매출"),
                () -> assertThat(summary.totalCommission().label()).isEqualTo("수수료"),
                () -> assertThat(summary.totalNetIncome().label()).isEqualTo("순수익")
        );
    }

    @Test
    @DisplayName("차트 데이터 구조 검증")
    void validateChartData_Success() {
        // When
        ArtistSettlementResponse response = createSampleResponse(LocalDateTime.now());
        ArtistSettlementResponse.Chart chart = response.chart();

        // Then - 차트 데이터 구조 검증
        assertAll(
                () -> assertThat(chart.series()).isNotNull(),
                () -> assertThat(chart.series().sales()).hasSize(3),
                () -> assertThat(chart.yDomain()).isNotNull(),
                () -> assertThat(chart.yDomain().min()).isEqualTo(0),
                () -> assertThat(chart.yDomain().max()).isPositive(),
                () -> assertThat(chart.series().sales().getFirst().bucketStart()).isNotBlank(),
                () -> assertThat(chart.series().sales().getFirst().value()).isNotNegative()
        );
    }

    @Test
    @DisplayName("테이블 데이터 구조와 편의 메서드 검증")
    void validateTableStructureAndConvenienceMethods_Success() {
        // When
        ArtistSettlementResponse response = createSampleResponse(LocalDateTime.now());
        ArtistSettlementResponse.Table table = response.table();

        // Then - 테이블 구조와 편의 메서드 검증
        assertAll(
                () -> assertThat(table.getContent()).hasSize(2),
                () -> assertThat(table.getPage()).isEqualTo(0),
                () -> assertThat(table.getSize()).isEqualTo(10),
                () -> assertThat(table.getTotalElements()).isPositive(),
                () -> assertThat(table.getTotalPages()).isPositive(),
                () -> assertThat(table.isHasNext()).isNotNull(),
                () -> assertThat(table.isHasPrevious()).isNotNull()
        );
    }

    @Test
    @DisplayName("정산 내역 개별 데이터 구조 검증")
    void validateSettlementItemStructure_Success() {
        // When
        ArtistSettlementResponse response = createSampleResponse(LocalDateTime.now());
        ArtistSettlementResponse.Settlement settlement = response.table().getContent().getFirst();

        // Then - 정산 내역 구조 검증
        assertAll(
                () -> assertThat(settlement.settlementId()).isPositive(),
                () -> assertThat(settlement.date()).isNotBlank(),
                () -> assertThat(settlement.product()).isNotNull(),
                () -> assertThat(settlement.product().id()).isPositive(),
                () -> assertThat(settlement.product().name()).isNotBlank(),
                () -> assertThat(settlement.grossAmount()).isNotNegative(),
                () -> assertThat(settlement.commission()).isNotNegative(),
                () -> assertThat(settlement.netAmount()).isNotNegative(),
                () -> assertThat(settlement.status()).isNotBlank(),
                () -> assertThat(settlement.statusText()).isNotBlank()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistSettlementResponse response = createApiResponse();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.scope().year()).isEqualTo(2025),
                () -> assertThat(response.scope().month()).isNull(),
                () -> assertThat(response.granularity()).isEqualTo("MONTH"),
                () -> assertThat(response.summary().totalSales().amount()).isEqualTo(128000),
                () -> assertThat(response.summary().totalCommission().amount()).isEqualTo(51264),
                () -> assertThat(response.summary().totalNetIncome().amount()).isEqualTo(64000),
                () -> assertThat(response.chart().series().sales()).hasSize(12), // 12개월 데이터
                () -> assertThat(response.table().getContent()).hasSize(4),
                () -> assertThat(response.table().getTotalElements()).isEqualTo(124)
        );
    }

    @Test
    @DisplayName("비즈니스 로직 검증 - 순수익 계산")
    void validateBusinessLogic_NetIncomeCalculation() {
        // When
        ArtistSettlementResponse response = createApiResponse();
        ArtistSettlementResponse.Settlement settlement = response.table().getContent().getFirst();

        // Then - 순수익 = 총 금액 - 수수료 검증
        int expectedNetAmount = settlement.grossAmount() - settlement.commission();
        assertThat(settlement.netAmount()).isEqualTo(expectedNetAmount);
    }

    // ---------- 헬퍼 메서드들 -------------

    private ArtistSettlementResponse createSampleResponse(LocalDateTime serverTime) {
        return new ArtistSettlementResponse(
                createSampleScope(),
                "MONTH",
                "Asia/Seoul",
                createSampleSummary(),
                createSampleChart(),
                createSampleTable(),
                serverTime
        );
    }

    private ArtistSettlementResponse createApiResponse() {
        // API 명세와 동일한 데이터 생성
        List<ArtistSettlementResponse.ChartDataPoint> salesData = Arrays.asList(
                new ArtistSettlementResponse.ChartDataPoint("2025-01-01", 500000),
                new ArtistSettlementResponse.ChartDataPoint("2025-02-01", 750000),
                new ArtistSettlementResponse.ChartDataPoint("2025-03-01", 650000),
                new ArtistSettlementResponse.ChartDataPoint("2025-04-01", 650000),
                new ArtistSettlementResponse.ChartDataPoint("2025-05-01", 550000),
                new ArtistSettlementResponse.ChartDataPoint("2025-06-01", 800000),
                new ArtistSettlementResponse.ChartDataPoint("2025-07-01", 850000),
                new ArtistSettlementResponse.ChartDataPoint("2025-08-01", 450000),
                new ArtistSettlementResponse.ChartDataPoint("2025-09-01", 800000),
                new ArtistSettlementResponse.ChartDataPoint("2025-10-01", 950000),
                new ArtistSettlementResponse.ChartDataPoint("2025-11-01", 1000000),
                new ArtistSettlementResponse.ChartDataPoint("2025-12-01", 1100000)
        );

        List<ArtistSettlementResponse.Settlement> settlements = Arrays.asList(
                new ArtistSettlementResponse.Settlement(
                        910004L, "2025-09-18",
                        new ArtistSettlementResponse.Product(101L, "상품명입니다 상품명입니다"),
                        18000, 200, 17800, "PENDING", "미지급"
                ),
                new ArtistSettlementResponse.Settlement(
                        910003L, "2025-09-18",
                        new ArtistSettlementResponse.Product(102L, "상품명입니다 상품명입니다"),
                        50000, 500, 49500, "COMPLETED", "정산 완료"
                ),
                new ArtistSettlementResponse.Settlement(
                        910002L, "2025-09-18",
                        new ArtistSettlementResponse.Product(103L, "상품명입니다 상품명입니다"),
                        30000, 1000, 29000, "COMPLETED", "정산 완료"
                ),
                new ArtistSettlementResponse.Settlement(
                        910001L, "2025-09-18",
                        new ArtistSettlementResponse.Product(104L, "상품명입니다 상품명입니다"),
                        5000, 100, 4900, "PENDING", "미지급"
                )
        );

        return new ArtistSettlementResponse(
                new ArtistSettlementResponse.Scope(2025, null),
                "MONTH",
                "Asia/Seoul",
                new ArtistSettlementResponse.Summary(
                        new ArtistSettlementResponse.AmountInfo(128000, "총 매출"),
                        new ArtistSettlementResponse.AmountInfo(51264, "수수료"),
                        new ArtistSettlementResponse.AmountInfo(64000, "순수익")
                ),
                new ArtistSettlementResponse.Chart(
                        new ArtistSettlementResponse.ChartSeries(salesData),
                        new ArtistSettlementResponse.YDomain(0, 1100000)
                ),
                new ArtistSettlementResponse.Table(
                        settlements, 0, 10, 124, 7, true, false
                ),
                LocalDateTime.of(2025, 9, 18, 15, 0)
        );
    }

    private ArtistSettlementResponse.Scope createSampleScope() {
        return new ArtistSettlementResponse.Scope(2025, null);
    }

    private ArtistSettlementResponse.Summary createSampleSummary() {
        return new ArtistSettlementResponse.Summary(
                new ArtistSettlementResponse.AmountInfo(100000, "총 매출"),
                new ArtistSettlementResponse.AmountInfo(10000, "수수료"),
                new ArtistSettlementResponse.AmountInfo(90000, "순수익")
        );
    }

    private ArtistSettlementResponse.Chart createSampleChart() {
        List<ArtistSettlementResponse.ChartDataPoint> dataPoints = Arrays.asList(
                new ArtistSettlementResponse.ChartDataPoint("2025-09-01", 30000),
                new ArtistSettlementResponse.ChartDataPoint("2025-09-15", 45000),
                new ArtistSettlementResponse.ChartDataPoint("2025-09-30", 25000)
        );

        return new ArtistSettlementResponse.Chart(
                new ArtistSettlementResponse.ChartSeries(dataPoints),
                new ArtistSettlementResponse.YDomain(0, 50000)
        );
    }

    private ArtistSettlementResponse.Table createSampleTable() {
        List<ArtistSettlementResponse.Settlement> settlements = Arrays.asList(
                new ArtistSettlementResponse.Settlement(
                        1001L, "2025-09-18",
                        new ArtistSettlementResponse.Product(1L, "테스트 상품 1"),
                        20000, 2000, 18000, "COMPLETED", "정산 완료"
                ),
                new ArtistSettlementResponse.Settlement(
                        1002L, "2025-09-17",
                        new ArtistSettlementResponse.Product(2L, "테스트 상품 2"),
                        15000, 1500, 13500, "PENDING", "미지급"
                )
        );

        return new ArtistSettlementResponse.Table(
                settlements, 0, 10, 50, 5, true, false
        );
    }
}
