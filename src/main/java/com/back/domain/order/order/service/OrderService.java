package com.back.domain.order.order.service;

import com.back.domain.cart.entity.Cart;
import com.back.domain.cart.repository.CartRepository;
import com.back.domain.order.order.dto.request.*;
import com.back.domain.order.order.dto.response.OrderResponseDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrder(User user, OrderRequestDto requestDto) {
        // 1. 주문상품 생성
        List<OrderItem> orderItems = createOrderItems(requestDto.orderItems());
        
        // 2. 주문 생성 (엔티티에서 처리)
        Order order = Order.createOrder(
                user, 
                orderItems,
                requestDto.shippingAddress1(),
                requestDto.shippingAddress2(),
                requestDto.shippingZip(),
                requestDto.recipientName(),
                requestDto.recipientPhone(),
                requestDto.deliveryRequest(),
                requestDto.paymentMethod()
        );
        
        // 3. 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        // 4. 장바구니에서 제거 (선택된 상품들)
        removeFromCart(user, requestDto.orderItems());
        
        return convertToOrderResponseDto(savedOrder);
    }

    /**
     * 주문 목록 조회 (페이징) - 모든 상품 상세 정보 포함
     */
    public Page<OrderResponseDto> getOrderList(User user, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user, pageable);
        
        return orders.map(order -> {
            // 주문 상세 조회와 동일한 로직 사용
            return convertToOrderResponseDto(order);
        });
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponseDto getOrderDetail(Long orderId, User user) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        return convertToOrderResponseDto(order);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId, User user, OrderCancelRequestDto requestDto) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        // 취소 가능 여부 체크
        if (order.getStatus() == OrderStatus.PREPARING_SHIPMENT || 
            order.getStatus() == OrderStatus.SHIPPING || 
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송준비중으로 상태가 변경된 이후에는 취소할 수 없습니다.");
        }
        
        // 재고 복원
        restoreStock(order);
        
        // 취소 실행
        order.cancel();
        orderRepository.save(order);
    }

    /**
     * 환불 신청
     */
    @Transactional
    public void requestRefund(Long orderId, User user, OrderRefundRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        // 환불 가능 여부 체크
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송완료된 주문만 환불 신청이 가능합니다.");
        }
        
        // 7일 체크
        LocalDateTime refundDeadline = order.getOrderDate().plusDays(7);
        if (LocalDateTime.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("배송완료 후 7일이 지나 환불 신청이 불가능합니다.");
        }
        
        // 환불 신청 상태 변경
        order.changeStatus(OrderStatus.REFUND_REQUESTED);
        orderRepository.save(order);
    }

    /**
     * 교환 신청
     */
    @Transactional
    public void requestExchange(Long orderId, User user, OrderExchangeRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        // 교환 가능 여부 체크
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송완료된 주문만 교환 신청이 가능합니다.");
        }
        
        // 7일 체크
        LocalDateTime exchangeDeadline = order.getOrderDate().plusDays(7);
        if (LocalDateTime.now().isAfter(exchangeDeadline)) {
            throw new IllegalStateException("배송완료 후 7일이 지나 교환 신청이 불가능합니다.");
        }
        
        // 교환 신청 상태 변경
        order.changeStatus(OrderStatus.EXCHANGE_REQUESTED);
        orderRepository.save(order);
    }

    /**
     * 주문 취소 승인 (관리자용)
     */
    @Transactional
    public void approveOrderCancellation(Long orderId, User admin) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (order.getStatus() != OrderStatus.CANCELLATION_REQUESTED) {
            throw new IllegalStateException("취소 요청된 주문만 승인할 수 있습니다.");
        }
        
        // 재고 복원
        restoreStock(order);
        
        // 취소 완료 처리
        order.completeCancellation();
        orderRepository.save(order);
    }

    /**
     * 주문 상태 변경 (관리자용)
     */
    @Transactional
    public void changeOrderStatus(Long orderId, OrderStatusChangeRequestDto requestDto, User admin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        order.changeStatus(requestDto.status());
        orderRepository.save(order);
    }

    // ==================== Private Methods ====================

    /**
     * 주문상품 생성
     */
    private List<OrderItem> createOrderItems(List<OrderRequestDto.OrderItemRequestDto> orderItemRequests) {
        return orderItemRequests.stream()
                .map(itemRequest -> {
                    Product product = productRepository.findByProductUuid(itemRequest.productUuid())
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
                    
                    // 재고 감소
                    int newStock = product.getStock() - itemRequest.quantity();
                    if (newStock < 0) {
                        throw new IllegalArgumentException("재고가 부족합니다. (상품: " + product.getName() + ", 현재 재고: " + product.getStock() + ")");
                    }
                    product.setStock(newStock);
                    
                    return OrderItem.createOrderItem(product, itemRequest.quantity(), itemRequest.optionInfo());
                })
                .toList();
    }

    /**
     * 장바구니에서 제거
     */
    private void removeFromCart(User user, List<OrderRequestDto.OrderItemRequestDto> orderItems) {
        List<UUID> productUuids = orderItems.stream()
                .map(OrderRequestDto.OrderItemRequestDto::productUuid)
                .toList();
        
        cartRepository.deleteByUserAndProductUuidIn(user, productUuids);
    }

    /**
     * 재고 복원 (주문 취소 시)
     */
    private void restoreStock(Order order) {
        order.getOrderItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            int restoredStock = product.getStock() + orderItem.getQuantity();
            product.setStock(restoredStock);
        });
    }

    /**
     * 상품 썸네일 이미지 URL 조회
     */
    private String getProductThumbnailUrl(Product product) {
        return product.getImages().stream()
                .filter(image -> "THUMBNAIL".equals(image.getFileType().name()))
                .findFirst()
                .map(image -> image.getFileUrl())
                .orElse(null);
    }

    /**
     * OrderResponseDto 변환
     */
    private OrderResponseDto convertToOrderResponseDto(Order order) {
        List<OrderResponseDto.OrderItemResponseDto> orderItemDtos = order.getOrderItems().stream()
                .map(item -> new OrderResponseDto.OrderItemResponseDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        getProductThumbnailUrl(item.getProduct()),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotalPrice(),
                        item.getOptionInfo()
                ))
                .toList();
        
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalQuantity(),
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getFinalAmount(),
                order.getShippingAddress1(),
                order.getShippingAddress2(),
                order.getShippingZip(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getDeliveryRequest(),
                order.getPaymentMethod(),
                order.getOrderDate(),
                orderItemDtos
        );
    }

}
