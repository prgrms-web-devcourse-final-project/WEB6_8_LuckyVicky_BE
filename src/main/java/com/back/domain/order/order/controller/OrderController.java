package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.request.OrderCancelRequestDto;
import com.back.domain.order.order.dto.request.OrderExchangeRequestDto;
import com.back.domain.order.order.dto.request.OrderRefundRequestDto;
import com.back.domain.order.order.dto.request.OrderRequestDto;
import com.back.domain.order.order.dto.request.OrderStatusChangeRequestDto;
import com.back.domain.order.order.dto.response.OrderResponseDto;
import com.back.domain.order.order.service.OrderService;
import com.back.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @PostMapping
    @Operation(summary = "주문 생성", description = "장바구니 상품을 주문으로 변환하여 주문을 생성합니다.")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        OrderResponseDto responseDto = orderService.createOrder(user, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 주문 목록 조회 (페이징) - 모든 상품 상세 정보 포함
     */
    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 목록을 조회합니다. 한 페이지에 8개씩 표시됩니다.")
    public ResponseEntity<Page<OrderResponseDto>> getOrderList(
            @PageableDefault(
                size = 8,
                sort = "orderDate",
                direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<OrderResponseDto> result = orderService.getOrderList(user, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    public ResponseEntity<OrderResponseDto> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user
    ) {
        OrderResponseDto result = orderService.getOrderDetail(orderId, user);
        return ResponseEntity.ok(result);
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "결제완료된 주문을 취소합니다.")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancelRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        orderService.cancelOrder(orderId, user, requestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 환불 신청
     */
    @PostMapping("/{orderId}/refund")
    @Operation(summary = "환불 신청", description = "배송완료된 주문에 대해 환불을 신청합니다.")
    public ResponseEntity<Void> requestRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRefundRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        orderService.requestRefund(orderId, user, requestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 교환 신청
     */
    @PostMapping("/{orderId}/exchange")
    @Operation(summary = "교환 신청", description = "배송완료된 주문에 대해 교환을 신청합니다.")
    public ResponseEntity<Void> requestExchange(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderExchangeRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        orderService.requestExchange(orderId, user, requestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 주문 상태 변경 (관리자용)
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "주문 상태 변경", description = "관리자가 주문 상태를 변경합니다. (관리자 전용)")
    public ResponseEntity<Void> changeOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusChangeRequestDto requestDto,
            @AuthenticationPrincipal User admin
    ) {
        orderService.changeOrderStatus(orderId, requestDto, admin);
        return ResponseEntity.ok().build();
    }
}