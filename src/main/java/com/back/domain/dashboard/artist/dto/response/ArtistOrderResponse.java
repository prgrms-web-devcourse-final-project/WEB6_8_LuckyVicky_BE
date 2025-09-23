package com.back.domain.dashboard.artist.dto.response;

import lombok.*;

import java.util.List;

/**
 * 작가 주문 내역 응답 DTO
 * 
 * 작가의 주문 관리 정보를 포함
 * 2025.09.24 생성
 */
public class ArtistOrderResponse {

    /**
     * 주문 목록 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class List {
        /** 요약 정보 */
        private Summary summary;
        /** 주문 목록 */
        private java.util.List<Order> content;
        /** 현재 페이지 */
        private int page;
        /** 페이지 크기 */
        private int size;
        /** 전체 요소 수 */
        private long totalElements;
        /** 전체 페이지 수 */
        private int totalPages;
        /** 다음 페이지 존재 여부 */
        private boolean hasNext;
        /** 이전 페이지 존재 여부 */
        private boolean hasPrevious;
    }

    /**
     * 주문 상태별 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /** 전체 주문 수 */
        private int total;
        /** 발주 전 */
        private int pending;
        /** 준비 중 */
        private int preparing;
        /** 배송 중 */
        private int shipped;
        /** 배송 완료 */
        private int delivered;
        /** 취소됨 */
        private int canceled;
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
        /** 주문 일자 */
        private String orderDate;
        /** 주문 상태 */
        private String status;
        /** 주문 상태 텍스트 */
        private String statusText;
        /** 총 주문 금액 */
        private int totalAmount;
        /** 상품 요약 */
        private String productSummary;
        /** 상품 개수 */
        private int itemCount;
        /** 구매자 정보 */
        private Buyer buyer;
        /** 배송 정보 */
        private Shipment shipment;
        /** 권한 정보 */
        private Permissions permissions;
    }

    /**
     * 구매자 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Buyer {
        /** 구매자 ID */
        private Long id;
        /** 구매자 닉네임 */
        private String nickname;
        /** 구매자 이름 */
        private String name;
    }

    /**
     * 배송 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shipment {
        /** 배송 상태 */
        private String status;
        /** 운송장 번호 */
        private String trackingNo;
        /** 택배사 */
        private String shippingCompany;
    }

    /**
     * 권한 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permissions {
        /** 상태 변경 가능 여부 */
        private boolean canChangeStatus;
        /** 취소 가능 여부 */
        private boolean canCancel;
    }
}
