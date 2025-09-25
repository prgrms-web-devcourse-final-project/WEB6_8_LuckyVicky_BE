package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

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
    public static class List extends PageResponse<OrderResponse.Summary> {
        /** 주문 현황 요약 정보 */
        private final SummaryDto summary;
        /** 시간대 정보 */
        private final String timezone;
        /** 기간 정보 */
        private final PeriodInfo period;
        
        public List() {
            super();
            this.summary = null;
            this.timezone = null;
            this.period = null;
        }
        
        public List(SummaryDto summary, java.util.List<Summary> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious, String timezone, PeriodInfo period) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
            this.timezone = timezone;
            this.period = period;
        }
        
        public SummaryDto getSummary() {
            return summary;
        }
        
        public String getTimezone() {
            return timezone;
        }
        
        public PeriodInfo getPeriod() {
            return period;
        }
    }
    
    /**
     * 주문 현황 요약 정보
     */
    public record SummaryDto(
            /** 전체 주문 건수 */
            int totalOrders,
            /** 결제완료 건수 */
            int pending,
            /** 발주 확인 건수 */
            int confirmed,
            /** 배송준비중 건수 */
            int preparing,
            /** 배송중 건수 */
            int shipped,
            /** 배송완료 건수 */
            int delivered,
            /** 취소된 건수 */
            int canceled,
            /** 취소 요청 건수 */
            int cancelRequested,
            /** 취소 처리중 건수 */
            int cancelProcessing,
            /** 취소 완료 건수 */
            int cancelCompleted,
            /** 교환 요청 건수 */
            int exchangeRequested,
            /** 교환 처리중 건수 */
            int exchangeProcessing,
            /** 교환 완료 건수 */
            int exchangeCompleted
    ) {}
    
    /**
     * 주문 요약 정보
     */
    public record Summary(
            String orderId,
            String orderNumber,
            String orderDate,
            String status,
            String statusText,
            int totalAmount,
            int itemCount,
            Product representativeItem,
            Shipping shipping,
            Aftersales aftersales,
            Permission permissions,
            Link links,
            java.util.List<OrderItem> items
    ) {}
    
    /**
     * 상품 정보
     */
    public record Product(
            Long productId,
            String productName,
            int quantity,
            int price,
            String imageUrl
    ) {}
    
    /**
     * 배송 정보
     */
    public record Shipping(
            /** 축약된 주소 */
            String addressShort,
            /** 수령인 */
            String recipient
    ) {}
    
    /**
     * A/S 정보
     */
    public record Aftersales(
            /** 취소 정보 */
            AftersalesItem cancel,
            /** 교환 정보 */
            AftersalesItem exchange
    ) {}
    
    /**
     * A/S 상세 정보
     */
    public record AftersalesItem(
            /** A/S 상태 */
            String status,
            /** A/S 상태 텍스트 */
            String statusText,
            /** A/S 요청 ID */
            Long requestId
    ) {}
    
    /**
     * 기간 정보
     */
    public record PeriodInfo(
            /** 기간 타입 */
            String type,
            /** 시작 날짜 */
            String from,
            /** 종료 날짜 */
            String to
    ) {}
    
    /**
     * 권한 정보
     */
    public record Permission(
            /** 취소 가능 여부 */
            Boolean canCancel,
            /** 반품 가능 여부 */
            Boolean canReturn,
            /** 교환 가능 여부 */
            Boolean canExchange
    ) {}
    
    /**
     * 링크 정보
     */
    public record Link(
            /** 상세 페이지 URL */
            String detail
    ) {}
    
    /**
     * 주문 상품 정보
     */
    public record OrderItem(
            Long orderItemId,
            Long productId,
            String productName,
            int quantity,
            int price,
            String imageUrl
    ) {}
}
