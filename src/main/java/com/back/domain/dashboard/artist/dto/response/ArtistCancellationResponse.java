package com.back.domain.dashboard.artist.dto.response;

/**
 * 작가 취소 요청 목록 응답 DTO
 * 
 * 작가의 주문 취소 요청 관리 정보를 포함
 * 2025.09.25 수정
 */
public class ArtistCancellationResponse {

    /**
     * 취소 요청 목록 응답
     */
    public record List(
            /** 요약 정보 */
            Summary summary,
            /** 취소 요청 목록 */
            java.util.List<CancellationRequest> content,
<<<<<<< HEAD
=======
            /** 일괄 작업 옵션 */
            java.util.List<BulkAction> bulkActions,
>>>>>>> e499333441d970159deadf1ba41779f3128785e8
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
     * 취소 요청 상태별 요약 정보
     */
    public record Summary(
            /** 전체 취소 요청 수 */
            int total,
            /** 처리 대기 */
            int pending,
            /** 승인됨 */
            int approved,
            /** 거절됨 */
            int rejected
    ) {}

    /**
     * 개별 취소 요청 정보
     */
    public record CancellationRequest(
            /** 요청 ID */
            Long requestId,
            /** 주문 ID */
            String orderId,
            /** 주문 번호 */
            String orderNumber,
            /** 요청 유형 */
            String type,
            /** 요청 상태 */
            String status,
            /** 요청 상태 텍스트 */
            String statusText,
            /** 요청 일시 */
            String requestDate,
            /** 취소 사유 */
            String reason,
            /** 고객 메시지 */
            String customerMessage,
            /** 고객 정보 */
            Customer customer,
            /** 주문 상품 정보 */
            OrderItem orderItem,
            /** 환불 금액 */
            int refundAmount,
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
     * 권한 정보
     */
    public record Permissions(
            /** 승인 가능 여부 */
            boolean canApprove,
            /** 거절 가능 여부 */
            boolean canReject
    ) {}
<<<<<<< HEAD
=======

    /**
     * 일괄 작업 정보
     */
    public record BulkAction(
            /** 작업 유형 */
            String action,
            /** 작업 라벨 */
            String label,
            /** 확인 필요 여부 */
            boolean requiresConfirmation
    ) {}
>>>>>>> e499333441d970159deadf1ba41779f3128785e8
}
