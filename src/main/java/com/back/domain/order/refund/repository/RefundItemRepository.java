package com.back.domain.order.refund.repository;

import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
    
    // 환불별 환불상품 조회
    List<RefundItem> findByRefund(Refund refund);
    
    // 주문상품별 환불상품 조회
    List<RefundItem> findByOrderItem(OrderItem orderItem);
}
