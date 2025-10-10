package com.back.domain.product.product.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * 상품 목록 조회 응답 DTO
 */
public record ProductListResponse(
        int page, // 현재 페이지 번호 (1부터 시작)
        int size,   // 한 페이지에 보여줄 상품 수
        long totalElements, // 전체 상품 수
        int totalPages,     // 전체 페이지 수
        List<ProductInfo> products // 상품 목록
) {
    // 단일 상품 정보 DTO
    public record ProductInfo(
            UUID productUuid, // productId → productUuid로 변경
            String url,  // 썸네일 이미지 URL
            String brandName,
            String name,
            int price,
            int discountRate,
            int discountPrice, // 할인 적용 가격
            Double rating  // (리뷰 연동 전) 아직 null
    ) {}
}

