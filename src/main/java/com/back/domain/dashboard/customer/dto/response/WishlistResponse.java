package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 찜하기 관련 응답 DTO
 *사용자가 찜한 상품들의 정보를 포함합니다.
 *2025.09.20 수정
 */
public class WishlistResponse {
    
    /**
     * 찜한 상품 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<WishlistResponse.Item> {
        /** 찜하기 현황 요약 정보 */
        private SummaryDto summary;
        /** 일괄 작업 옵션 */
        private java.util.List<BulkAction> bulkActions;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<BulkAction> bulkActions, 
                   java.util.List<Item> content, int page, int size, 
                   long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
            this.bulkActions = bulkActions;
        }
    }
    
    /**
     * 찜하기 현황 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        /** 전체 찜한 상품 수 */
        private int totalWishItems;
    }
    
    /**
     * 찜한 상품 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String wishId;
        private Long productId;
        private String productNumber;
        private String productName;
        private int price;
        private Artist artist;
        private String imageUrl;
        private String sellingStatus;
        private String registeredDate;
        private LocalDateTime addedAt;
        private String productPageUrl;
        private Permission permissions;
    }
    
    /**
     * 작가 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        private String id;
        private String name;
    }
    
    /**
     * 권한 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        /** 찜 해제 가능 여부 */
        private Boolean canUnwish;
    }
    
    /**
     * 일괄 작업 옵션
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkAction {
        /** 작업 종류 */
        private String action;
        /** 표시 라벨 */
        private String label;
        /** 확인 필요 여부 */
        private Boolean requiresConfirmation;
    }
}
