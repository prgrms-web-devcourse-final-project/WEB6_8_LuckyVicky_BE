package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 관련 응답 DTO
 */
public class OrderResponse {
    
    @Getter
    @Setter
    public static class List extends PageResponse<OrderResponse.Summary> {
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private int totalOrders;
        private int pending;
        private int preparing;
        private int shipped;
        private int delivered;
        private int canceled;
    }
    
    @Getter
    @Setter
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private Long productId;
        private String productName;
        private int quantity;
        private int price;
        private String imageUrl;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shipping {
        private String addressShort;
        private String recipient;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private Boolean canCancel;
        private Boolean canReturn;
        private Boolean canExchange;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        private String detail;
    }
    
    @Getter
    @Setter
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
