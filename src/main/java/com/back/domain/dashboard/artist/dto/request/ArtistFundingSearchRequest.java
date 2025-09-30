package com.back.domain.dashboard.artist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

/**
 * 작가 펀딩 목록 검색 요청 DTO
 * 2025.09.30 수정 - status 값을 FundingStatus enum에 맞게 수정, sort 옵션 조정
 */
public record ArtistFundingSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 검색 키워드 (펀딩 제목) */
        String keyword,

        /** 펀딩 상태 필터 (OPEN, CLOSED, SUCCESS, FAILED, CANCELED) */
        @Pattern(regexp = "^(OPEN|CLOSED|SUCCESS|FAILED|CANCELED)$",
                message = "status는 OPEN, CLOSED, SUCCESS, FAILED, CANCELED 중 하나여야 합니다")
        String status,

        /** 카테고리 ID 필터 (현재 미사용) */
        @Positive(message = "카테고리 ID는 양수여야 합니다")
        Long categoryId,

        /** 최소 달성률 (%) */
        @Min(value = 0, message = "최소 달성률은 0 이상이어야 합니다")
        @Max(value = 10000, message = "최소 달성률은 10000 이하여야 합니다")
        Integer minAchievement,

        /** 최대 달성률 (%) */
        @Min(value = 0, message = "최대 달성률은 0 이상이어야 합니다")
        @Max(value = 10000, message = "최대 달성률은 10000 이하여야 합니다")
        Integer maxAchievement,

        /** 시작 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String startDate,

        /** 종료 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String endDate,

        /** 정렬 기준 (endDate, startDate, createDate, title) */
        @Pattern(regexp = "^(endDate|startDate|createDate|title)$",
                message = "sort는 endDate, startDate, createDate, title 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ArtistFundingSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sort == null) sort = "endDate";
        if (order == null) order = "ASC";
    }
}
