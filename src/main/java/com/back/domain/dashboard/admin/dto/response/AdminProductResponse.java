package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 상품 목록 조회 응답 DTO
 *
 * 관리자가 전체 상품을 조회하고 관리할 수 있는 정보를 포함
 * 2025.09.26 생성
 * 2025.10.01 Summary 제거 (화면에서 사용하지 않음)
 */
public record AdminProductResponse(
        /** 상품 목록 */
        List<Product> content,
        /** 페이지 번호 */
        int page,
        /** 페이지 크기 */
        int size,
        /** 전체 요소 수 */
        long totalElements,
        /** 전체 페이지 수 */
        int totalPages,
        /** 다음 페이지 존재 여부 */
        boolean hasNext,
        /** 이전 페이지 존재 여부 */
        boolean hasPrevious
) {

    /**
     * 상품 정보
     */
    public record Product(
            /** 상품 ID (기본키) */
            Long productId,
            /** 상품 번호 */
            String productNumber,
            /** 상품명 */
            String name,
            /** 작가명 */
            String artistName,
            /** 판매 상태 */
            String sellingStatus,
            /** 등록일 */
            LocalDate registeredAt
    ) {}
}
