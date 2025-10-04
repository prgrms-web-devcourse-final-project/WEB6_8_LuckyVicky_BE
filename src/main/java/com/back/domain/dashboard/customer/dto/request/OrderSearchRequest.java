package com.back.domain.dashboard.customer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 주문 목록 검색 요청 DTO
 * 2025.10.03 수정 - 검색 키워드 및 정렬 기능 중심으로 변경
 */
public record OrderSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 검색 키워드 (상품명 또는 주문번호) */
        String keyword,

        /** 정렬 기준 */
        @Pattern(regexp = "^(orderDate|productName|totalAmount|status)$",
                message = "sort는 orderDate, productName, totalAmount, status 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public OrderSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sort == null) sort = "orderDate";
        if (order == null) order = "DESC";
    }
}
