package com.back.domain.order.exchange.repository;

import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.order.entity.Order;
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
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    // 주문별 교환 조회
    List<Exchange> findByOrder(Order order);
    
    // 사용자별 교환 조회
    List<Exchange> findByUser(User user);
    
    // 상태별 교환 조회
    List<Exchange> findByStatus(Exchange.ExchangeStatus status);
    
    // 사용자별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByUserOrderByCreateDateDesc(User user, Pageable pageable);
    
    // 주문별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByOrderOrderByCreateDateDesc(Order order, Pageable pageable);
    
    // 상태별 교환 목록 (페이징, 최신순)
    Page<Exchange> findByStatusOrderByCreateDateDesc(Exchange.ExchangeStatus status, Pageable pageable);
    
    // N+1 문제 해결: 교환과 교환 아이템을 함께 조회
    @Query("SELECT e FROM Exchange e LEFT JOIN FETCH e.exchangeItems ei LEFT JOIN FETCH ei.orderItem oi LEFT JOIN FETCH oi.product p LEFT JOIN FETCH p.images WHERE e.id = :exchangeId")
    Optional<Exchange> findByIdWithItems(@Param("exchangeId") Long exchangeId);
    
    // N+1 문제 해결: 사용자별 교환 목록과 아이템들을 함께 조회
    @Query("SELECT e FROM Exchange e LEFT JOIN FETCH e.exchangeItems ei LEFT JOIN FETCH ei.orderItem oi LEFT JOIN FETCH oi.product p LEFT JOIN FETCH p.images WHERE e.user = :user ORDER BY e.createDate DESC")
    List<Exchange> findByUserWithItems(@Param("user") User user);
}
