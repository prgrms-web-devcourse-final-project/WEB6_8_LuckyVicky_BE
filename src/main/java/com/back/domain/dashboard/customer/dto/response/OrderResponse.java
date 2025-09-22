package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 관련 응답 DTO
 * 
 * 사용자의 주문 내역과 관련된 모든 정보를 포함합니다.
 * 2025.09.22 수정 - 애프터세일즈 상태 및 기간 필터 추가
 */
public class OrderResponse {
    
    /**
     * 주문 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<OrderResponse.Order> {
        /** 주문 현황 요약 정보 */
        private SummaryDto summary;
        /** 타임존 */
        private String timezone;
        /** 조회 기간 정보 */
        private Period period;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Order> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious, String timezone, Period period) {
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
        
        // 주문 상태별 카운트
        private int pending;          // 대기중
        private int confirmed;        // 확정
        private int preparing;        // 준비중
        private int shipped;          // 배송중
        private int delivered;        // 배송완료
        private int canceled;         // 취소됨
        
        // 취소 관련 상태별 카운트
        private int cancelRequested;   // 취소 요청
        private int cancelProcessing;  // 취소 처리중
        private int cancelCompleted;   // 취소 완료
        
        // 교환 관련 상태별 카운트
        private int exchangeRequested;  // 교환 요청
        private int exchangeProcessing; // 교환 처리중
        private int exchangeCompleted;  // 교환 완료
    }
    
    /**
     * 개별 주문 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        /** 주문 ID */
        private String orderId;
        /** 주문 번호 */
        private String orderNumber;
        /** 주문 일시 */
        private LocalDateTime orderDate;
        /** 주문 상태 */
        private String status;
        /** 주문 상태 텍스트 */
        private String statusText;
        /** 총 주문 금액 */
        private int totalAmount;
        /** 상품 개수 */
        private int itemCount;
        /** 대표 상품 정보 */
        private RepresentativeItem representativeItem;
        /** 배송 정보 */
        private Shipping shipping;
        /** 애프터세일즈 정보 */
        private Aftersales aftersales;
        /** 권한 정보 */
        private Permission permissions;
        /** 링크 정보 */
        private Link links;
        /** 주문 상품 목록 (선택적) */
        private java.util.List<OrderItem> items;
    }
    
    /**
     * 대표 상품 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepresentativeItem {
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
     * 애프터세일즈 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aftersales {
        /** 취소 정보 */
        private AftersalesInfo cancel;
        /** 교환 정보 */
        private AftersalesInfo exchange;
    }
    
    /**
     * 애프터세일즈 개별 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AftersalesInfo {
        /** 상태 */
        private String status;
        /** 상태 텍스트 */
        private String statusText;
        /** 요청 ID */
        private Long requestId;
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
    
    /**
     * 조회 기간 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        /** 기간 타입 */
        private String type;
        /** 시작일 */
        private String from;
        /** 종료일 */
        private String to;
    }
}
