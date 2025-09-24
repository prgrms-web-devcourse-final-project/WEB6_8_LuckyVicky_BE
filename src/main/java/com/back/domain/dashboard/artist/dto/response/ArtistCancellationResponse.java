package com.back.domain.dashboard.artist.dto.response;

import lombok.*;

import java.util.List;

/**
 * 작가 취소 요청 목록 응답 DTO
 * 
 * 작가의 주문 취소 요청 관리 정보를 포함
 * 2025.09.24 생성
 */
public class ArtistCancellationResponse {

    /**
     * 취소 요청 목록 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class List {
        /** 요약 정보 */
        private Summary summary;
        /** 취소 요청 목록 */
        private java.util.List<CancellationRequest> content;
        /** 일괄 작업 옵션 */
        private java.util.List<BulkAction> bulkActions;
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
     * 취소 요청 상태별 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /** 전체 취소 요청 수 */
        private int total;
        /** 처리 대기 */
        private int pending;
        /** 승인됨 */
        private int approved;
        /** 거절됨 */
        private int rejected;
    }

    /**
     * 개별 취소 요청 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancellationRequest {
        /** 요청 ID */
        private Long requestId;
        /** 주문 ID */
        private String orderId;
        /** 주문 번호 */
        private String orderNumber;
        /** 요청 유형 */
        private String type;
        /** 요청 상태 */
        private String status;
        /** 요청 상태 텍스트 */
        private String statusText;
        /** 요청 일시 */
        private String requestDate;
        /** 취소 사유 */
        private String reason;
        /** 고객 메시지 */
        private String customerMessage;
        /** 고객 정보 */
        private Customer customer;
        /** 주문 상품 정보 */
        private OrderItem orderItem;
        /** 환불 금액 */
        private int refundAmount;
        /** 권한 정보 */
        private Permissions permissions;
    }

    /**
     * 고객 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        /** 고객 ID */
        private Long id;
        /** 고객 닉네임 */
        private String nickname;
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
        /** 상품 ID */
        private Long productId;
        /** 상품명 */
        private String productName;
        /** 수량 */
        private int quantity;
        /** 가격 */
        private int price;
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
        /** 승인 가능 여부 */
        private boolean canApprove;
        /** 거절 가능 여부 */
        private boolean canReject;
    }

    /**
     * 일괄 작업 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkAction {
        /** 작업 유형 */
        private String action;
        /** 작업 라벨 */
        private String label;
        /** 확인 필요 여부 */
        private boolean requiresConfirmation;
    }
}
