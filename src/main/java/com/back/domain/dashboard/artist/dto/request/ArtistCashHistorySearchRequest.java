package com.back.domain.dashboard.artist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 작가 캐시 입금/환전 내역 검색 요청 DTO
 * 2025.09.25 생성
 */
public record ArtistCashHistorySearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 거래 유형 필터 */
        @Pattern(regexp = "^(DEPOSIT|WITHDRAWAL)$",
                message = "type은 DEPOSIT 또는 WITHDRAWAL 중 하나여야 합니다")
        String type,

        /** 거래 상태 필터 */
        @Pattern(regexp = "^(PENDING|PROCESSING|COMPLETED|FAILED|CANCELED)$",
                message = "status는 PENDING, PROCESSING, COMPLETED, FAILED, CANCELED 중 하나여야 합니다")
        String status,

        /** 시작 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String dateFrom,

        /** 종료 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String dateTo,

        /** 정렬 기준 */
        @Pattern(regexp = "^(transactedAt|amount|type|status)$",
                message = "sort는 transactedAt, amount, type, status 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ArtistCashHistorySearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sort == null) sort = "transactedAt";
        if (order == null) order = "DESC";
    }
}
