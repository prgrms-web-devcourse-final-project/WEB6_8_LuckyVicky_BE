package com.back.domain.dashboard.artist.dto.response;

/**
 * 작가 주문 내역 응답 DTO
 * 
 * 작가의 주문 관리 정보를 포함
 * 2025.09.25 수정
 */
public class ArtistOrderResponse {

    /**
     * 주문 목록 응답
     */
    public record List(
            /** 요약 정보 */
            Summary summary,
            /** 주문 목록 */
            java.util.List<Order> content,
            /** 현재 페이지 */
            int page,
            /** 페이지 크기 */
            int size,
            /** 전체 요소 수 */
            long totalElements,
            /** 전체 페이지 수 */
            int totalPages,
            /** 다음 페이지 존재 여부 */
            boolean hasNext,
            /** 이전 페이지 존재 여부 */
            boolean hasPrevious
    ) {}

    /**
     * 주문 상태별 요약 정보
     */
    public record Summary(
            /** 전체 주문 수 */
            int total,
            /** 발주 전 */
            int pending,
            /** 준비 중 */
            int preparing,
            /** 배송 중 */
            int shipped,
            /** 배송 완료 */
            int delivered,
            /** 취소됨 */
            int canceled
    ) {}

    /**
     * 개별 주문 정보
     */
    public record Order(
            /** 주문 ID */
            String orderId,
            /** 주문 번호 */
            String orderNumber,
            /** 주문 일자 */
            String orderDate,
            /** 주문 상태 */
            String status,
            /** 주문 상태 텍스트 */
            String statusText,
            /** 총 주문 금액 */
            int totalAmount,
            /** 상품 요약 */
            String productSummary,
            /** 상품 개수 */
            int itemCount,
            /** 구매자 정보 */
            Buyer buyer,
            /** 배송 정보 */
            Shipment shipment,
            /** 권한 정보 */
            Permissions permissions
    ) {}

    /**
     * 구매자 정보
     */
    public record Buyer(
            /** 구매자 ID */
            Long id,
            /** 구매자 닉네임 */
            String nickname,
            /** 구매자 이름 */
            String name
    ) {}

    /**
     * 배송 정보
     */
    public record Shipment(
            /** 배송 상태 */
            String status,
            /** 운송장 번호 */
            String trackingNo,
            /** 택배사 */
            String shippingCompany
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /** 상태 변경 가능 여부 */
            boolean canChangeStatus,
            /** 취소 가능 여부 */
            boolean canCancel
    ) {}
}
