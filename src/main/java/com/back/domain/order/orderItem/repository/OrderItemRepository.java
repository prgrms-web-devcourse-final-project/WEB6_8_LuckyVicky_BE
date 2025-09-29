package com.back.domain.order.orderItem.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.orderItem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // 주문상품 조회 (상품 정보만)
    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN FETCH oi.product " +
           "WHERE oi.order = :order")
    List<OrderItem> findByOrderWithProduct(@Param("order") Order order);
}
