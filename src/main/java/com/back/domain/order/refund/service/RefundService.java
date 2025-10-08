package com.back.domain.order.refund.service;

import com.back.domain.order.common.service.BaseOrderActionService;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.order.refund.dto.request.RefundRequestDto;
import com.back.domain.order.refund.dto.response.RefundResponseDto;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.entity.RefundItem;
import com.back.domain.order.refund.repository.RefundItemRepository;
import com.back.domain.order.refund.repository.RefundRepository;
import com.back.domain.order.refund.util.RefundConverter;
import com.back.domain.product.product.entity.Product;
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
public class RefundService extends BaseOrderActionService<Refund, RefundResponseDto, RefundItem> {

    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;
    private final RefundConverter refundConverter;

    public RefundService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        RefundRepository refundRepository, RefundItemRepository refundItemRepository,
                        RefundConverter refundConverter) {
        super(orderRepository, orderItemRepository);
        this.refundRepository = refundRepository;
        this.refundItemRepository = refundItemRepository;
        this.refundConverter = refundConverter;
    }

    /**
     * 환불 신청
     */
    @Transactional
    public RefundResponseDto createRefund(RefundRequestDto requestDto, User user) {
        log.info("환불 신청 시작 - 사용자: {}, 주문 ID: {}", user.getId(), requestDto.orderId());
        
        // 1. 공통 검증 로직 사용
        Order order = validateOrderAndOwnership(requestDto.orderId(), user);
        List<OrderItem> orderItems = validateOrderItems(requestDto.orderItemIds());
        
        // 2. 환불 가능 여부 검증 (Order 엔티티에서 처리)
        order.requestRefund();
        
        // 3. 환불 엔티티 생성 (팩토리 메서드 사용)
        Refund refund = Refund.createRefund(
            order,
            user,
            requestDto.reasonType(), // 환불 사유 타입 추가
            requestDto.reason(),
            requestDto.detailReason(),
            requestDto.refundAmount(),
            requestDto.refundMethod(),
            requestDto.attachmentFiles() != null ? String.join(",", requestDto.attachmentFiles()) : null
        );
        
        // 4. 환불 저장
        Refund savedRefund = refundRepository.save(refund);
        
        // 5. 환불 아이템 생성 및 저장
        List<RefundItem> refundItems = orderItems.stream()
                .map(orderItem -> RefundItem.builder()
                        .refund(savedRefund)
                        .orderItem(orderItem)
                        .quantity(orderItem.getQuantity())
                        .refundPrice(orderItem.getPrice())
                        .build())
                .toList();
        
        refundItemRepository.saveAll(refundItems);
        
        log.info("환불 신청 완료 - 환불 ID: {}", savedRefund.getId());
        
        // 6. N+1 문제 해결: 한 번의 쿼리로 모든 데이터 조회
        Refund refundWithItems = refundRepository.findByIdWithItems(savedRefund.getId())
                .orElseThrow(() -> new IllegalArgumentException("환불 정보를 찾을 수 없습니다."));
        
        return refundConverter.toResponseDto(refundWithItems, refundWithItems.getRefundItems());
    }

    /**
     * 환불 상세 조회 (N+1 문제 해결)
     */
    @Override
    public RefundResponseDto getItem(Long refundId, User user) {
        log.info("환불 상세 조회 - 환불 ID: {}, 사용자: {}", refundId, user.getId());
        
        Refund refund = refundRepository.findByIdWithItems(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 정보를 찾을 수 없습니다."));
        
        if (!refund.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 환불에 대한 권한이 없습니다.");
        }
        
        return refundConverter.toResponseDto(refund, refund.getRefundItems());
    }

    /**
     * 사용자별 환불 목록 조회 (N+1 문제 해결)
     */
    @Override
    public Page<RefundResponseDto> getItemsByUser(User user, Pageable pageable) {
        log.info("사용자별 환불 목록 조회 - 사용자: {}", user.getId());
        
        // N+1 문제 해결: 한 번의 쿼리로 모든 데이터 조회
        List<Refund> refunds = refundRepository.findByUserWithItems(user);
        
        // 수동으로 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), refunds.size());
        List<Refund> pagedRefunds = start >= refunds.size() ? List.of() : refunds.subList(start, end);
        
        List<RefundResponseDto> refundDtos = pagedRefunds.stream()
                .map(refund -> refundConverter.toResponseDto(refund, refund.getRefundItems()))
                .toList();
        
        return new PageImpl<>(refundDtos, pageable, refunds.size());
    }

    /**
     * 환불 승인
     */
    @Override
    @Transactional
    public RefundResponseDto approveItem(Long refundId, User admin) {
        log.info("환불 승인 - 환불 ID: {}, 관리자: {}", refundId, admin.getId());
        
        Refund refund = refundRepository.findByIdWithItems(refundId)
                .orElseThrow(() -> new IllegalArgumentException("환불 정보를 찾을 수 없습니다."));
        
        if (refund.getStatus() != Refund.RefundStatus.REQUESTED) {
            throw new IllegalStateException("승인 대기 중인 환불만 승인할 수 있습니다.");
        }
        
        // 재고 복원 여부 확인 (사유 타입에 따라 자동 판단)
        if (refund.getReasonType().shouldRestoreStock()) {
            log.info("환불 사유가 '{}'이므로 재고를 복원합니다.", refund.getReasonType().getDescription());
            restoreStockForRefund(refund);
        } else {
            log.info("환불 사유가 '{}'이므로 재고를 복원하지 않습니다.", refund.getReasonType().getDescription());
        }
        
        // 환불 승인 처리
        refund.approve();
        
        // 주문 상태 변경
        refund.getOrder().completeRefund();
        
        log.info("환불 승인 완료 - 환불 ID: {}", refundId);
        
        return refundConverter.toResponseDto(refund, refund.getRefundItems());
    }

    /**
     * 환불 시 재고 복원
     */
    private void restoreStockForRefund(Refund refund) {
        refund.getRefundItems().forEach(refundItem -> {
            Product product = refundItem.getOrderItem().getProduct();
            int restoredStock = product.getStock() + refundItem.getQuantity();
            product.setStock(restoredStock);
            log.info("재고 복원 - 상품: {}, 복원 수량: {}, 복원 후 재고: {}", 
                    product.getName(), refundItem.getQuantity(), restoredStock);
        });
    }
}