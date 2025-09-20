package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 찜하기 관련 응답 DTO
 */
public class WishlistResponse {
    
    @Getter
    @Setter
    public static class List extends PageResponse<WishlistResponse.Item> {
        private SummaryDto summary;
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private int totalWishItems;
    }
    
    @Getter
    @Setter
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        private String id;
        private String name;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private Boolean canUnwish;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkAction {
        private String action;
        private String label;
        private Boolean requiresConfirmation;
    }
}
