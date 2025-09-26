package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 상품 목록 조회 요청 DTO
 * 2025.09.26 신규 생성
 */
public record AdminProductSearchRequest(
        /** 페이지 번호 (0-based) */
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 통합 검색 키워드 (상품명/상품번호/작가명) */
        String keyword,

        /** 판매 상태 */
        @Pattern(regexp = "^(SELLING|STOPPED)$",
                message = "sellingStatus는 SELLING 또는 STOPPED여야 합니다")
        String sellingStatus,

        /** 카테고리 ID */
        Long categoryId,

        /** 작가 ID */
        Long artistId,

        /** 등록일 시작 (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "startDate는 yyyy-MM-dd 형식이어야 합니다")
        String startDate,

        /** 등록일 종료 (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "endDate는 yyyy-MM-dd 형식이어야 합니다")
        String endDate,

        /** 정렬 기준 */
        @Pattern(regexp = "^(productNumber|name|artistName|sellingStatus|registeredAt)$",
                message = "sort는 productNumber, name, artistName, sellingStatus, registeredAt 중 하나여야 합니다")
        String sort,

        /** 정렬 순서 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order,

        /** 메트릭 포함 여부 (평균평점/리뷰/매출) */
        Boolean metrics
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminProductSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sort == null) sort = "registeredAt";
        if (order == null) order = "DESC";
        if (metrics == null) metrics = false;
    }
}
