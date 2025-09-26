package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 상품 목록 조회 응답 DTO
 *
 * 관리자가 전체 상품을 조회하고 관리할 수 있는 정보를 포함
 * 2025.09.26 생성
 */
public record AdminProductResponse(
        /** 상품 요약 정보 */
        Summary summary,
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
     * 상품 요약 정보
     */
    public record Summary(
            /** 전체 상품 수 */
            int totalProducts,
            /** 판매중 상품 수 */
            int sellingProducts,
            /** 판매중지 상품 수 */
            int stoppedProducts
    ) {}

    /**
     * 상품 정보
     */
    public record Product(
            /** 상품 ID */
            long productId,
            /** 상품 번호 */
            String productNumber,
            /** 상품명 */
            String name,
            /** 작가 정보 */
            Artist artist,
            /** 판매 상태 */
            String sellingStatus,
            /** 카테고리 정보 */
            Category category,
            /** 등록일 */
            LocalDate registeredAt,
            /** 권한 정보 */
            Permissions permissions,
            /** 평균 평점 (metrics=true일 때만) */
            Double averageRating,
            /** 리뷰 수 (metrics=true일 때만) */
            Integer reviewCount,
            /** 매출 (metrics=true일 때만) */
            Long revenue,
            /** 모더레이션 상태 (선택) */
            Moderation moderation
    ) {}

    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID */
            long id,
            /** 작가명 */
            String name
    ) {}

    /**
     * 카테고리 정보
     */
    public record Category(
            /** 카테고리 ID */
            long id,
            /** 카테고리명 */
            String name
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /** 중재 권한 */
            boolean moderate,
            /** 삭제 권한 */
            boolean delete,
            /** 상태 변경 권한 */
            boolean statusChange
    ) {}

    /**
     * 모더레이션 정보
     */
    public record Moderation(
            /** 대기중인 요청 존재 여부 */
            boolean hasPendingRequest,
            /** 마지막 요청 일시 */
            LocalDate lastRequestedAt
    ) {}
}
