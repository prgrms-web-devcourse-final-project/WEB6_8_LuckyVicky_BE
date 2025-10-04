package com.back.domain.order.common.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 주문 관련 공통 서비스 (환불/교환 공통 로직)
 */
@Slf4j
@Transactional(readOnly = true)
public abstract class BaseOrderActionService<T, R, I> {

    protected final OrderRepository orderRepository;
    protected final OrderItemRepository orderItemRepository;

    protected BaseOrderActionService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 주문 조회 및 권한 체크 (공통 로직)
     */
    protected Order validateOrderAndOwnership(Long orderId, User user) {
        log.info("주문 조회 및 권한 체크 - orderId: {}, userId: {}", orderId, user.getId());
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 주문에 대한 권한이 없습니다.");
        }
        
        return order;
    }

    /**
     * 주문상품 조회 및 검증 (공통 로직)
     */
    protected List<OrderItem> validateOrderItems(List<Long> orderItemIds) {
        log.info("주문상품 조회 및 검증 - orderItemIds: {}", orderItemIds);
        
        List<OrderItem> orderItems = orderItemRepository.findAllById(orderItemIds);
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("해당 상품을 찾을 수 없습니다.");
        }
        
        return orderItems;
    }

    /**
     * 사용자별 목록 조회 (공통 패턴)
     */
    public abstract Page<R> getItemsByUser(User user, Pageable pageable);

    /**
     * 상세 조회 (공통 패턴)
     */
    public abstract R getItem(Long itemId, User user);

    /**
     * 승인 처리 (공통 패턴)
     */
    @Transactional
    public abstract R approveItem(Long itemId, User admin);
}
