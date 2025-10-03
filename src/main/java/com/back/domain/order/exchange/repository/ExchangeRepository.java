package com.back.domain.order.exchange.repository;

import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.order.entity.Order;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    // 주문별 교환 조회
    List<Exchange> findByOrder(Order order);
    
    // 사용자별 교환 조회
    List<Exchange> findByUser(User user);
    
    // 상태별 교환 조회
    List<Exchange> findByStatus(Exchange.ExchangeStatus status);
    
    // 사용자별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 주문별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByOrderOrderByCreatedAtDesc(Order order, Pageable pageable);
    
    // 상태별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByStatusOrderByCreatedAtDesc(Exchange.ExchangeStatus status, Pageable pageable);
}
