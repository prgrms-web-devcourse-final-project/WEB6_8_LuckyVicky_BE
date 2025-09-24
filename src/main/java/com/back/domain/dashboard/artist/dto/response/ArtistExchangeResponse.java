package com.back.domain.dashboard.artist.dto.response;

import com.back.global.util.PageResponse;
import lombok.*;

import java.util.List;

/**
 * 작가 교환 요청 관련 응답 DTO
 *
 * 작가의 교환 요청 목록과 관련된 정보를 포함합니다.
 * 2025.09.24 생성
 */
public class ArtistExchangeResponse {

    /**
     * 교환 요청 목록 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class List {
        /** 교환 요청 요약 정보 */
        private Summary summary;
        /** 교환 요청 목록 */
        private java.util.List<ExchangeRequest> content;
        /** 일괄 작업 옵션 */
        private java.util.List<BulkAction> bulkActions;
        /** 페이지 번호 */
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
     * 교환 요청 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /** 전체 교환 요청 수 */
        private int total;
        /** 처리 대기 수 */
        private int pending;
        /** 승인된 수 */
        private int approved;
        /** 거절된 수 */
        private int rejected;
    }

    /**
     * 교환 요청 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeRequest {
        /** 교환 요청 ID */
        private Long requestId;
        /** 주문 ID */
        private String orderId;
        /** 주문 번호 */
        private String orderNumber;
        /** 요청 타입 */
        private String type;
        /** 상태 */
        private String status;
        /** 상태 텍스트 */
        private String statusText;
        /** 요청 일시 */
        private String requestDate;
        /** 교환 사유 */
        private String reason;
        /** 고객 메시지 */
        private String customerMessage;
        /** 고객 정보 */
        private Customer customer;
        /** 주문 상품 정보 */
        private OrderItem orderItem;
        /** 교환 요청 정보 */
        private ExchangeRequested exchangeRequested;
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
     * 교환 요청 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeRequested {
        /** 교환 옵션 */
        private String option;
        /** 교환 수량 */
        private int quantity;
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
     * 일괄 작업 옵션
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkAction {
        /** 액션 코드 */
        private String action;
        /** 액션 라벨 */
        private String label;
        /** 확인 필요 여부 */
        private boolean requiresConfirmation;
    }
}
