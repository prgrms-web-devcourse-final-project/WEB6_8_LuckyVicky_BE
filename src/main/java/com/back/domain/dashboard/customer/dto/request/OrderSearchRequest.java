package com.back.domain.dashboard.customer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 주문 목록 검색 요청 DTO
 * 2025.09.25 생성
 */
public record OrderSearchRequest(
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

        /** A/S 상태 필터 */
        @Pattern(regexp = "^(CANCEL_REQUESTED|CANCEL_PROCESSING|CANCEL_COMPLETED|EXCHANGE_REQUESTED|EXCHANGE_PROCESSING|EXCHANGE_COMPLETED)$", 
                 message = "aftersalesStatus는 CANCEL_REQUESTED, CANCEL_PROCESSING, CANCEL_COMPLETED, EXCHANGE_REQUESTED, EXCHANGE_PROCESSING, EXCHANGE_COMPLETED 중 하나여야 합니다")
        String aftersalesStatus,

        /** 시작 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", 
                 message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String from,

        /** 종료 날짜 */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", 
                 message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
        String to,

        /** 기간 필터 */
        @Pattern(regexp = "^(TODAY|WEEK|MONTH|QUARTER|YEAR|CUSTOM)$", 
                 message = "period는 TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM 중 하나여야 합니다")
        String period,

        /** 정렬 기준 */
        @Pattern(regexp = "^(orderDate|orderNumber|status|totalAmount)$", 
                 message = "sort는 orderDate, orderNumber, status, totalAmount 중 하나여야 합니다")
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
        if (period == null) period = "MONTH";
        if (sort == null) sort = "orderDate";
        if (order == null) order = "DESC";
    }
}
