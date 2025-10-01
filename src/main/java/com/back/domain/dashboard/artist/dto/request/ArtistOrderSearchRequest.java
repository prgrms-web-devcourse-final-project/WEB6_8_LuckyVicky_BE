package com.back.domain.dashboard.artist.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 작가 주문 내역 검색 요청 DTO
 * 2025.09.25 생성
 */
public record ArtistOrderSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 주문 상태 필터 */
        @Pattern(regexp = "^(PENDING|CONFIRMED|PREPARING|SHIPPED|DELIVERED|CANCELED)$",
                message = "status는 PENDING, CONFIRMED, PREPARING, SHIPPED, DELIVERED, CANCELED 중 하나여야 합니다")
        String status,

        /** 검색 키워드 (주문번호, 고객명, 상품명) */
        String keyword,

        /** 시작 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String startDate,

        /** 종료 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String endDate,

        /** 정렬 기준 */
        @Pattern(regexp = "^(orderDate|orderNumber|status|totalAmount|customerName)$",
                message = "sort는 orderDate, orderNumber, status, totalAmount, customerName 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public ArtistOrderSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sort == null) sort = "orderDate";
        if (order == null) order = "DESC";
    }
}
