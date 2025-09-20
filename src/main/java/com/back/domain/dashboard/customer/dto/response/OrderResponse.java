package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

/**
 * 주문 관련 응답 DTO
 * 
 *사용자의 주문 내역과 관련된 모든 정보를 포함합니다.
 *2025.09.20 수정
 */
public class OrderResponse {
    
    /**
     * 주문 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<OrderResponse.Summary> {
        /** 주문 현황 요약 정보 */
        private SummaryDto summary;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Summary> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
    }
    
    /**
     * 주문 현황 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        /** 전체 주문 건수 */
        private int totalOrders;
        /** 결제완료 건수 */
        private int pending;
        /** 배송준비중 건수 */
        private int preparing;
        /** 배송중 건수 */
        private int shipped;
        /** 배송완료 건수 */
        private int delivered;
        /** 취소된 건수 */
        private int canceled;
    }
    
    /**
     * 주문 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String orderId;
        private String orderNumber;
        private String orderDate;
        private String status;
        private String statusText;
        private int totalAmount;
        private int itemCount;
        private Product representativeItem;
        private Shipping shipping;
        private Permission permissions;
        private Link links;
        private java.util.List<OrderItem> items;
    }
    
    /**
     * 상품 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private Long productId;
        private String productName;
        private int quantity;
        private int price;
        private String imageUrl;
    }
    
    /**
     * 배송 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shipping {
        /** 축약된 주소 */
        private String addressShort;
        /** 수령인 */
        private String recipient;
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
        /** 취소 가능 여부 */
        private Boolean canCancel;
        /** 반품 가능 여부 */
        private Boolean canReturn;
        /** 교환 가능 여부 */
        private Boolean canExchange;
    }
    
    /**
     * 링크 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        /** 상세 페이지 URL */
        private String detail;
    }
    
    /**
     * 주문 상품 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long orderItemId;
        private Long productId;
        private String productName;
        private int quantity;
        private int price;
        private String imageUrl;
    }
}
