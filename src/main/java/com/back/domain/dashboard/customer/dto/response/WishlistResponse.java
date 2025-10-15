package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import java.time.LocalDateTime;

/**
 * 찜하기 관련 응답 DTO
 * 사용자가 찜한 상품들의 정보를 포함
 * 2025.09.22 수정
 */
public class WishlistResponse {

    /**
     * 찜한 상품 목록 응답
     */
    public static class List extends PageResponse<WishlistResponse.Item> {
        /**
         * 찜하기 현황 요약 정보
         */
        private final SummaryDto summary;
        /**
         * 일괄 작업 옵션
         */
        private final java.util.List<BulkAction> bulkActions;

        public List() {
            super();
            this.summary = null;
            this.bulkActions = null;
        }

        public List(SummaryDto summary, java.util.List<BulkAction> bulkActions,
                    java.util.List<Item> content, int page, int size,
                    long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
            this.bulkActions = bulkActions;
        }

        public SummaryDto getSummary() {
            return summary;
        }

        public java.util.List<BulkAction> getBulkActions() {
            return bulkActions;
        }
    }

    /**
     * 찜하기 현황 요약 정보
     */
    public record SummaryDto(
            /** 전체 찜한 상품 수 */
            int totalWishItems
    ) {
    }

    /**
     * 찜한 상품 정보
     */
    public record Item(
            String wishId,
            Long productId,
            String productNumber,
            String brandName,
            String productName,
            int price,
            Artist artist,
            String imageUrl,
            String sellingStatus,
            LocalDateTime addedAt,
            String productPageUrl,
            Permission permissions
    ) {
    }

    /**
     * 작가 정보
     */
    public record Artist(
            String id,
            String name
    ) {
    }

    /**
     * 권한 정보
     */
    public record Permission(
            /** 찜 해제 가능 여부 */
            Boolean canUnwish
    ) {
    }

    /**
     * 일괄 작업 옵션
     */
    public record BulkAction(
            /** 작업 종류 */
            String action,
            /** 표시 라벨 */
            String label,
            /** 확인 필요 여부 */
            Boolean requiresConfirmation
    ) {
    }
}
