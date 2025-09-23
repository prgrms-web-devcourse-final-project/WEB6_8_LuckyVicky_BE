package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

/**
 * 주문 관련 응답 DTO
 * 
 *사용자의 주문 내역과 관련된 모든 정보를 포함
 *2025.09.23 수정
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
        /** 시간대 정보 */
        private String timezone;
        /** 기간 정보 */
        private PeriodInfo period;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Summary> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious, String timezone, PeriodInfo period) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
            this.timezone = timezone;
            this.period = period;
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
        /** 발주 확인 건수 */
        private int confirmed;
        /** 배송준비중 건수 */
        private int preparing;
        /** 배송중 건수 */
        private int shipped;
        /** 배송완료 건수 */
        private int delivered;
        /** 취소된 건수 */
        private int canceled;
        
        /** 취소 요청 건수 */
        private int cancelRequested;
        /** 취소 처리중 건수 */
        private int cancelProcessing;
        /** 취소 완료 건수 */
        private int cancelCompleted;
        
        /** 교환 요청 건수 */
        private int exchangeRequested;
        /** 교환 처리중 건수 */
        private int exchangeProcessing;
        /** 교환 완료 건수 */
        private int exchangeCompleted;
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
        private Aftersales aftersales;
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
     * A/S 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aftersales {
        /** 취소 정보 */
        private AftersalesItem cancel;
        /** 교환 정보 */
        private AftersalesItem exchange;
    }
    
    /**
     * A/S 상세 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AftersalesItem {
        /** A/S 상태 */
        private String status;
        /** A/S 상태 텍스트 */
        private String statusText;
        /** A/S 요청 ID */
        private Long requestId;
    }
    
    /**
     * 기간 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodInfo {
        /** 기간 타입 */
        private String type;
        /** 시작 날짜 */
        private String from;
        /** 종료 날짜 */
        private String to;
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
