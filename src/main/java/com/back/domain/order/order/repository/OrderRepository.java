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

    /**
     * 대시보드용 주문 목록 조회 (검색 + 정렬 + 페이징)
     * 상품명으로 검색
     * 4가지 배송 상태만 조회 (결제완료, 배송준비중, 배송중, 배송완료)
     * 취소/교환/환불 상태는 제외
     * 동적 정렬 지원
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE o.user = :user " +
            "AND o.status IN (com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED, " +
            "                 com.back.domain.order.order.entity.OrderStatus.PREPARING_SHIPMENT, " +
            "                 com.back.domain.order.order.entity.OrderStatus.SHIPPING, " +
            "                 com.back.domain.order.order.entity.OrderStatus.DELIVERED) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     EXISTS (SELECT 1 FROM OrderItem oi2 WHERE oi2.order = o AND oi2.product.name LIKE %:keyword%))")
    Page<Order> findOrdersForDashboard(
            @Param("user") User user,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}