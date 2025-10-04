package com.back.domain.order.refund.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.refund.entity.Refund;
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
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    // 주문별 환불 조회
    List<Refund> findByOrder(Order order);
    
    // 사용자별 환불 조회
    List<Refund> findByUser(User user);
    
    // 상태별 환불 조회
    List<Refund> findByStatus(Refund.RefundStatus status);
    
    // 사용자별 환불 목록 (페이징, 최신순)
    Page<Refund> findByUserOrderByCreateDateDesc(User user, Pageable pageable);
    
    // 주문별 환불 목록 (페이징, 최신순)
    Page<Refund> findByOrderOrderByCreateDateDesc(Order order, Pageable pageable);
    
    // 상태별 환불 목록 (페이징, 최신순)
    Page<Refund> findByStatusOrderByCreateDateDesc(Refund.RefundStatus status, Pageable pageable);
    
    // N+1 문제 해결: 환불과 환불 아이템을 함께 조회
    @Query("SELECT r FROM Refund r LEFT JOIN FETCH r.refundItems ri LEFT JOIN FETCH ri.orderItem oi LEFT JOIN FETCH oi.product p LEFT JOIN FETCH p.images WHERE r.id = :refundId")
    Optional<Refund> findByIdWithItems(@Param("refundId") Long refundId);
    
    // N+1 문제 해결: 사용자별 환불 목록과 아이템들을 함께 조회
    @Query("SELECT r FROM Refund r LEFT JOIN FETCH r.refundItems ri LEFT JOIN FETCH ri.orderItem oi LEFT JOIN FETCH oi.product p LEFT JOIN FETCH p.images WHERE r.user = :user ORDER BY r.createDate DESC")
    List<Refund> findByUserWithItems(@Param("user") User user);
}
