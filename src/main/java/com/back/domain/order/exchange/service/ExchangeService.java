package com.back.domain.order.exchange.service;

import com.back.domain.order.common.service.BaseOrderActionService;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.order.exchange.dto.request.ExchangeRequestDto;
import com.back.domain.order.exchange.dto.response.ExchangeResponseDto;
import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeItem;
import com.back.domain.order.exchange.repository.ExchangeItemRepository;
import com.back.domain.order.exchange.repository.ExchangeRepository;
import com.back.domain.order.exchange.util.ExchangeConverter;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ExchangeService extends BaseOrderActionService<Exchange, ExchangeResponseDto, ExchangeItem> {

    private final ExchangeRepository exchangeRepository;
    private final ExchangeItemRepository exchangeItemRepository;
    private final ExchangeConverter exchangeConverter;

    public ExchangeService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                          ExchangeRepository exchangeRepository, ExchangeItemRepository exchangeItemRepository,
                          ExchangeConverter exchangeConverter) {
        super(orderRepository, orderItemRepository);
        this.exchangeRepository = exchangeRepository;
        this.exchangeItemRepository = exchangeItemRepository;
        this.exchangeConverter = exchangeConverter;
    }

    /**
     * 교환 신청
     */
    @Transactional
    public ExchangeResponseDto createExchange(ExchangeRequestDto requestDto, User user) {
        log.info("교환 신청 시작 - 사용자: {}, 주문 ID: {}", user.getId(), requestDto.orderId());
        
        // 1. 공통 검증 로직 사용
        Order order = validateOrderAndOwnership(requestDto.orderId(), user);
        List<OrderItem> orderItems = validateOrderItems(requestDto.orderItemIds());
        
        // 2. 교환 가능 여부 검증 (Order 엔티티에서 처리)
        order.requestExchange();
        
        // 3. 교환 엔티티 생성 (팩토리 메서드 사용)
        Exchange exchange = Exchange.createExchange(
            order,
            user,
            requestDto.reason(),
            requestDto.detailReason(),
            requestDto.exchangeMethod(),
            requestDto.attachmentFiles() != null ? String.join(",", requestDto.attachmentFiles()) : null,
            requestDto.newShippingAddress1(),
            requestDto.newShippingAddress2(),
            requestDto.newShippingZip(),
            requestDto.newRecipientName(),
            requestDto.newRecipientPhone()
        );
        
        // 4. 교환 저장
        Exchange savedExchange = exchangeRepository.save(exchange);
        
        // 5. 교환 아이템 생성 및 저장
        List<ExchangeItem> exchangeItems = orderItems.stream()
                .map(orderItem -> ExchangeItem.builder()
                        .exchange(savedExchange)
                        .orderItem(orderItem)
                        .quantity(orderItem.getQuantity())
                        .build())
                .toList();
        
        exchangeItemRepository.saveAll(exchangeItems);
        
        log.info("교환 신청 완료 - 교환 ID: {}", savedExchange.getId());
        
        // 6. N+1 문제 해결: 한 번의 쿼리로 모든 데이터 조회
        Exchange exchangeWithItems = exchangeRepository.findByIdWithItems(savedExchange.getId())
                .orElseThrow(() -> new IllegalArgumentException("교환 정보를 찾을 수 없습니다."));
        
        return exchangeConverter.toResponseDto(exchangeWithItems, exchangeWithItems.getExchangeItems());
    }

    /**
     * 교환 상세 조회 (N+1 문제 해결)
     */
    @Override
    public ExchangeResponseDto getItem(Long exchangeId, User user) {
        log.info("교환 상세 조회 - 교환 ID: {}, 사용자: {}", exchangeId, user.getId());
        
        Exchange exchange = exchangeRepository.findByIdWithItems(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("교환 정보를 찾을 수 없습니다."));
        
        if (!exchange.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 교환에 대한 권한이 없습니다.");
        }
        
        return exchangeConverter.toResponseDto(exchange, exchange.getExchangeItems());
    }

    /**
     * 사용자별 교환 목록 조회 (N+1 문제 해결)
     */
    @Override
    public Page<ExchangeResponseDto> getItemsByUser(User user, Pageable pageable) {
        log.info("사용자별 교환 목록 조회 - 사용자: {}", user.getId());
        
        // N+1 문제 해결: 한 번의 쿼리로 모든 데이터 조회
        List<Exchange> exchanges = exchangeRepository.findByUserWithItems(user);
        
        // 수동으로 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), exchanges.size());
        List<Exchange> pagedExchanges = start >= exchanges.size() ? List.of() : exchanges.subList(start, end);
        
        List<ExchangeResponseDto> exchangeDtos = pagedExchanges.stream()
                .map(exchange -> exchangeConverter.toResponseDto(exchange, exchange.getExchangeItems()))
                .toList();
        
        return new PageImpl<>(exchangeDtos, pageable, exchanges.size());
    }

    /**
     * 교환 승인
     */
    @Override
    @Transactional
    public ExchangeResponseDto approveItem(Long exchangeId, User admin) {
        log.info("교환 승인 - 교환 ID: {}, 관리자: {}", exchangeId, admin.getId());
        
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("교환 정보를 찾을 수 없습니다."));
        
        if (exchange.getStatus() != Exchange.ExchangeStatus.REQUESTED) {
            throw new IllegalStateException("승인 대기 중인 교환만 승인할 수 있습니다.");
        }
        
        // 교환 승인 처리
        exchange.approve();
        
        // 주문 상태 변경
        exchange.getOrder().completeExchange();
        
        log.info("교환 승인 완료 - 교환 ID: {}", exchangeId);
        
        // N+1 문제 해결: 한 번의 쿼리로 모든 데이터 조회
        Exchange exchangeWithItems = exchangeRepository.findByIdWithItems(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("교환 정보를 찾을 수 없습니다."));
        
        return exchangeConverter.toResponseDto(exchangeWithItems, exchangeWithItems.getExchangeItems());
    }
}