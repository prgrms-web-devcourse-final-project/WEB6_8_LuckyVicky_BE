package com.back.domain.order.exchange.repository;

import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeItem;
import com.back.domain.order.orderItem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {
    
    // 교환별 교환상품 조회
    List<ExchangeItem> findByExchange(Exchange exchange);
    
    // 주문상품별 교환상품 조회
    List<ExchangeItem> findByOrderItem(OrderItem orderItem);
}
