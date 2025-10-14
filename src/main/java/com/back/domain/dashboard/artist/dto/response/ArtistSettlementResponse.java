package com.back.domain.dashboard.artist.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작가 정산내역 응답 DTO
 *
 * 정산내역의 범위, 요약, 차트, 테이블 정보를 포함
 * 2025.09.25 생성
 */
public record ArtistSettlementResponse(
        /** 조회 범위 */
        Scope scope,
        /** 타임존 */
        String timezone,
        /** 요약 정보 */
        Summary summary,
        /** 차트 데이터 */
        Chart chart,
        /** 테이블 데이터 */
        Table table,
        /** 서버 시간 */
        LocalDateTime serverTime
) {

    /**
     * 조회 범위 정보
     */
    public record Scope(
            /** 연도 */
            Integer year,
            /** 월 */
            Integer month
    ) {}

    /**
     * 요약 정보
     */
    public record Summary(
            /** 총 매출 */
            AmountInfo totalSales,
            /** 총 수수료 */
            AmountInfo totalCommission,
            /** 순수익 */
            AmountInfo totalNetIncome
    ) {}

    /**
     * 금액 정보
     */
    public record AmountInfo(
            /** 금액 */
            Integer amount,
            /** 라벨 */
            String label
    ) {}

    /**
     * 차트 데이터
     */
    public record Chart(
            /** 시계열 데이터 */
            ChartSeries series,
            /** Y축 범위 */
            YDomain yDomain
    ) {}

    /**
     * 차트 시계열 데이터
     */
    public record ChartSeries(
            /** 매출 데이터 */
            List<ChartDataPoint> sales
    ) {}

    /**
     * 차트 데이터 포인트
     */
    public record ChartDataPoint(
            /** 기간 시작점 */
            String bucketStart,
            /** 값 */
            Integer value
    ) {}

    /**
     * Y축 범위
     */
    public record YDomain(
            /** 최소값 */
            Integer min,
            /** 최대값 */
            Integer max
    ) {}

    /**
     * 테이블 데이터
     */
    public record Table(
            /** 정산 내역 목록 */
            List<Settlement> content,
            /** 페이지 번호 */
            Integer page,
            /** 페이지 크기 */
            Integer size,
            /** 전체 요소 수 */
            Integer totalElements,
            /** 전체 페이지 수 */
            Integer totalPages,
            /** 다음 페이지 존재 여부 */
            Boolean hasNext,
            /** 이전 페이지 존재 여부 */
            Boolean hasPrevious
    ) {
        // 편의 메서드들
        public List<Settlement> getContent() { return content; }
        public Integer getPage() { return page; }
        public Integer getSize() { return size; }
        public Integer getTotalElements() { return totalElements; }
        public Integer getTotalPages() { return totalPages; }
        public Boolean isHasNext() { return hasNext; }
        public Boolean isHasPrevious() { return hasPrevious; }
    }

    /**
     * 정산 정보
     */
    public record Settlement(
            /** 정산 ID */
            Long settlementId,
            /** 정산 날짜 */
            String date,
            /** 상품 정보 */
            Product product,
            /** 총 금액 */
            Integer grossAmount,
            /** 수수료 */
            Integer commission,
            /** 순 금액 */
            Integer netAmount,
            /** 상태 */
            String status,
            /** 상태 텍스트 */
            String statusText
    ) {}

    /**
     * 상품 정보
     */
    public record Product(
            /** 상품 ID */
            Long id,
            /** 상품명 */
            String name
    ) {}
}
