package com.back.domain.dashboard.artist.dto.response;

/**
 * 작가 교환 요청 관련 응답 DTO
 *
 * 작가의 교환 요청 목록과 관련된 정보를 포함합니다.
 * 2025.09.25 수정
 */
public class ArtistExchangeResponse {

    /**
     * 교환 요청 목록 응답
     */
    public record List(
            /** 교환 요청 요약 정보 */
            Summary summary,
            /** 교환 요청 목록 */
            java.util.List<ExchangeRequest> content,
            /** 페이지 번호 */
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
     * 교환 요청 요약 정보
     */
    public record Summary(
            /** 전체 교환 요청 수 */
            int total,
            /** 처리 대기 수 */
            int pending,
            /** 승인된 수 */
            int approved,
            /** 거절된 수 */
            int rejected
    ) {}

    /**
     * 교환 요청 정보
     */
    public record ExchangeRequest(
            /** 교환 요청 ID */
            Long requestId,
            /** 주문 ID */
            String orderId,
            /** 주문 번호 */
            String orderNumber,
            /** 요청 타입 */
            String type,
            /** 상태 */
            String status,
            /** 상태 텍스트 */
            String statusText,
            /** 요청 일시 */
            String requestDate,
            /** 교환 사유 */
            String reason,
            /** 고객 메시지 */
            String customerMessage,
            /** 고객 정보 */
            Customer customer,
            /** 주문 상품 정보 */
            OrderItem orderItem,
            /** 교환 요청 정보 */
            ExchangeRequested exchangeRequested,
            /** 권한 정보 */
            Permissions permissions
    ) {}

    /**
     * 고객 정보
     */
    public record Customer(
            /** 고객 ID */
            Long id,
            /** 고객 닉네임 */
            String nickname
    ) {}

    /**
     * 주문 상품 정보
     */
    public record OrderItem(
            /** 상품 ID */
            Long productId,
            /** 상품명 */
            String productName,
            /** 수량 */
            int quantity,
            /** 가격 */
            int price
    ) {}

    /**
     * 교환 요청 정보
     */
    public record ExchangeRequested(
            /** 교환 옵션 */
            String option,
            /** 교환 수량 */
            int quantity
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /** 승인 가능 여부 */
            boolean canApprove,
            /** 거절 가능 여부 */
            boolean canReject
    ) {}
}
