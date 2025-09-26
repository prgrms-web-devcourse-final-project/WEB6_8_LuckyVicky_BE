package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 매출/정산 집계 응답 DTO
 * 
 * 관리자 대시보드의 매출/정산 집계 데이터를 포함
 * 2025.09.26 생성
 */
public record AdminSettlementResponse(
        /** 조회 범위 */
        Scope scope,
        /** 집계 단위 */
        String granularity,
        /** 타임존 */
        String timezone,
        /** 요약 정보 */
        Summary summary,
        /** 차트 데이터 */
        Chart chart,
        /** 테이블 데이터 */
        List<TableRow> table,
        /** 서버 시간 */
        LocalDateTime serverTime
) {

    /**
     * 조회 범위
     */
    public record Scope(
            /** 연도 */
            int year,
            /** 월 (null이면 연도 전체) */
            Integer month
    ) {}

    /**
     * 요약 정보
     */
    public record Summary(
            /** 총 매출액 */
            long totalGrossSales,
            /** 총 작가 정산금 */
            long totalArtistPayout,
            /** 총 순수익 */
            long totalNetIncome
    ) {}

    /**
     * 차트 데이터
     */
    public record Chart(
            /** 시계열 데이터 */
            Series series
    ) {}

    /**
     * 시계열 데이터
     */
    public record Series(
            /** 매출 데이터 */
            List<DataPoint> grossSales,
            /** 작가 정산 데이터 */
            List<DataPoint> artistPayout,
            /** 순수익 데이터 */
            List<DataPoint> netIncome
    ) {}

    /**
     * 데이터 포인트
     */
    public record DataPoint(
            /** 기간 시작일 */
            String bucketStart,
            /** 값 */
            long value
    ) {}

    /**
     * 테이블 행
     */
    public record TableRow(
            /** 기간 시작일 */
            String bucketStart,
            /** 매출 */
            long grossSales,
            /** 작가 정산금 */
            long artistPayout,
            /** 순수익 */
            long netIncome
    ) {}
}
