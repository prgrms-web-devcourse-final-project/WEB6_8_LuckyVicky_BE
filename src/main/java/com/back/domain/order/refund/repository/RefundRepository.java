package com.back.domain.order.refund.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    // 주문별 환불 조회
    List<Refund> findByOrder(Order order);
    
    // 사용자별 환불 조회
    List<Refund> findByUser(User user);
    
    // 상태별 환불 조회
    List<Refund> findByStatus(Refund.RefundStatus status);
    
    // 사용자별 환불 목록 (페이징, 최신순)
    Page<Refund> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 주문별 환불 목록 (페이징, 최신순)
    Page<Refund> findByOrderOrderByCreatedAtDesc(Order order, Pageable pageable);
    
    // 상태별 환불 목록 (페이징, 최신순)
    Page<Refund> findByStatusOrderByCreatedAtDesc(Refund.RefundStatus status, Pageable pageable);
}
