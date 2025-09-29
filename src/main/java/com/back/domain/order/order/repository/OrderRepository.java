package com.back.domain.order.order.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 사용자별 주문 목록 (페이징)
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);
    
    // 주문 상세 조회 - Fetch Join (상품 정보만)
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);
    
    // 주문번호로 조회
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // 상태별 조회 (페이징)
    Page<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status, Pageable pageable);
    
    // 단순 목록 조회
    List<Order> findByUserOrderByOrderDateDesc(User user);
}