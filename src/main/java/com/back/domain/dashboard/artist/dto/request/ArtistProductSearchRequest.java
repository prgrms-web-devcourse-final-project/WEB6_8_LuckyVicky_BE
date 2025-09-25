package com.back.domain.dashboard.artist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 작가 상품 목록 검색 요청 DTO
 * 2025.09.25 생성
 */
public record ArtistProductSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 검색 키워드 (상품명) */
        String keyword,

        /** 판매 상태 필터 */
        Boolean selling,

        /** 정렬 기준 */
        @Pattern(regexp = "^(registrationDate|productName|price|status|salesCount)$",
                message = "sort는 registrationDate, productName, price, status, salesCount 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ArtistProductSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sort == null) sort = "registrationDate";
        if (order == null) order = "DESC";
    }
}
