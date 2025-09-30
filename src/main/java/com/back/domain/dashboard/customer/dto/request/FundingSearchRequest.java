package com.back.domain.dashboard.customer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 참여한 펀딩 목록 검색 요청 DTO
 * 2025.09.30 수정 - Funding 엔티티에 맞춰 정렬 필드 수정 (pledgedDate → paidAt)
 */
public record FundingSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 펀딩 상태 필터 */
        @Pattern(regexp = "^(ACTIVE|ENDED|FULFILLING|FULFILLED)$", 
                 message = "status는 ACTIVE, ENDED, FULFILLING, FULFILLED 중 하나여야 합니다")
        String status,

        /** 검색 키워드 (펀딩 제목/작가명) */
        String keyword,

        /** 정렬 기준 */
        @Pattern(regexp = "^(paidAt|pledgedAmount|title|artistName|status)$", 
                 message = "sort는 paidAt, pledgedAmount, title, artistName, status 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$", 
                 message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public FundingSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sort == null) sort = "paidAt";
        if (order == null) order = "DESC";
    }
}
