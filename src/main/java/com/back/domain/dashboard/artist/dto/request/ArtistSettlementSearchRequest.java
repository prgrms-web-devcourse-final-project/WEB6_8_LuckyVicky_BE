package com.back.domain.dashboard.artist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 작가 정산내역 조회 요청 DTO
 * 2025.09.25 생성
 */
public record ArtistSettlementSearchRequest(
        /** 조회 연도 */
        @Min(value = 2020, message = "연도는 2020년 이후만 가능합니다")
        Integer year,

        /** 조회 월 (1-12) */
        @Min(value = 1, message = "월은 1-12 사이여야 합니다")
        @Max(value = 12, message = "월은 1-12 사이여야 합니다")
        Integer month,

        /** 집계 단위 */
        @Pattern(regexp = "^(MONTH|DAY)$",
                message = "granularity는 MONTH, DAY 중 하나여야 합니다")
        String granularity,

        /** 정산 상태 */
        @Pattern(regexp = "^(PENDING|PROCESSING|COMPLETED)$",
                message = "status는 PENDING, PROCESSING, COMPLETED 중 하나여야 합니다")
        String status,

        /** 상품 ID */
        Long productId,

        /** 페이지 번호 */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 */
        @Min(value = 1, message = "크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "크기는 100 이하여야 합니다")
        Integer size,

        /** 정렬 기준 */
        @Pattern(regexp = "^(date|grossAmount|commission|netAmount|status)$",
                message = "sort는 date, grossAmount, commission, netAmount, status 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC, DESC 중 하나여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ArtistSettlementSearchRequest {
        if (granularity == null) granularity = "MONTH";
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sort == null) sort = "date";
        if (order == null) order = "DESC";
    }
}
