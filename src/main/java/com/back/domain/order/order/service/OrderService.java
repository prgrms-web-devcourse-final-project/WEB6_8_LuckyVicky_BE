package com.back.domain.order.order.service;

import com.back.domain.cart.repository.CartRepository;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.order.order.dto.request.*;
import com.back.domain.order.order.dto.response.OrderResponseDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final FundingRepository fundingRepository;
    private final CartRepository cartRepository;
    private final NotificationService notificationService;
    private final com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository moriCashBalanceRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrder(User user, OrderRequestDto requestDto) {
        // 1. 주문상품 생성
        List<OrderItem> orderItems = createOrderItems(requestDto.orderItems());
        
        // 2. 재고 부족 알림 발송 (재고 감소는 이미 createOrderItems에서 처리됨)
        sendLowStockNotifications(orderItems);
        
        // 3. 주문 생성 (엔티티에서 처리)
        Order order = Order.createOrder(
                user, 
                orderItems,
                requestDto.shippingAddress1(),
                requestDto.shippingAddress2(),
                requestDto.shippingZip(),
                requestDto.recipientName(),
                requestDto.recipientPhone(),
                requestDto.deliveryRequest(),
                requestDto.paymentMethod()
        );
        
        // 4. 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        // 5. 장바구니에서 제거 (선택된 상품들)
        removeFromCart(user, requestDto.orderItems());
        
        // 6. 알림 발송 - 사용자: 주문 확정
        notificationService.sendNotification(
            user,
            NotificationType.ORDER_CONFIRMED,
            "주문이 확정되었습니다. 주문번호: " + savedOrder.getOrderNumber(),
            "/mypage/orders/" + savedOrder.getId()
        );
        
        // 7. 알림 발송 - 작가: 새로운 주문 (NORMAL/FUNDING 상품의 작가에게)
        sendNewOrderNotifications(orderItems, savedOrder);
        
        return convertToOrderResponseDto(savedOrder);
    }

    /**
     * 주문 목록 조회 (페이징) - 모든 상품 상세 정보 포함
     */
    public Page<OrderResponseDto> getOrderList(User user, Pageable pageable) {
        // N+1 방지를 위해 Fetch Join 사용
        List<Order> orders = orderRepository.findByUserWithOrderItemsAndProducts(user, pageable);
        
        // 수동으로 페이징 처리
        long totalCount = orderRepository.countByUser(user);
        Page<Order> orderPage = new org.springframework.data.domain.PageImpl<>(orders, pageable, totalCount);
        
        return orderPage.map(this::convertToOrderResponseDto);
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderDetail(Long orderId, User user) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        return convertToOrderResponseDto(order);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId, User user, OrderCancelRequestDto requestDto) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        // 취소 가능 여부 체크
        if (order.getStatus() == OrderStatus.PREPARING_SHIPMENT || 
            order.getStatus() == OrderStatus.SHIPPING || 
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송준비중으로 상태가 변경된 이후에는 취소할 수 없습니다.");
        }
        
        // 재고 복원
        restoreStock(order);
        
        // 취소 실행
        order.cancel();
        orderRepository.save(order);
        
        // 알림 발송 - 사용자: 주문 취소
        notificationService.sendNotification(
            user,
            NotificationType.ORDER_CANCELLED,
            "주문이 취소되었습니다. 주문번호: " + order.getOrderNumber(),
            "/mypage/orders/" + order.getId()
        );
        
        // 알림 발송 - 작가: 주문 취소 (각 상품의 작가에게)
        order.getOrderItems().stream()
            .map(OrderItem::getProduct)
            .map(Product::getUser)
            .distinct()
            .forEach(artist -> {
                notificationService.sendNotification(
                    artist,
                    NotificationType.ORDER_CANCELLED_SELLER,
                    "주문이 취소되었습니다. 주문번호: " + order.getOrderNumber(),
                    "/artist/orders/" + order.getId()
                );
            });
    }

    /**
     * 환불 신청
     */
    @Transactional
    public void requestRefund(Long orderId, User user, OrderRefundRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        // 환불 가능 여부 체크
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송완료된 주문만 환불 신청이 가능합니다.");
        }
        
        // 7일 체크
        LocalDateTime refundDeadline = order.getOrderDate().plusDays(7);
        if (LocalDateTime.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("배송완료 후 7일이 지나 환불 신청이 불가능합니다.");
        }
        
        // 환불 신청 상태 변경
        order.changeStatus(OrderStatus.REFUND_REQUESTED);
        orderRepository.save(order);
        
        // 알림 발송은 환불 완료 시에만 발송 (관리자 승인 후)
    }

    /**
     * 교환 신청
     */
    @Transactional
    public void requestExchange(Long orderId, User user, OrderExchangeRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        order.validateOwnership(user);
        
        // 교환 가능 여부 체크
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송완료된 주문만 교환 신청이 가능합니다.");
        }
        
        // 7일 체크
        LocalDateTime exchangeDeadline = order.getOrderDate().plusDays(7);
        if (LocalDateTime.now().isAfter(exchangeDeadline)) {
            throw new IllegalStateException("배송완료 후 7일이 지나 교환 신청이 불가능합니다.");
        }
        
        // 교환 신청 상태 변경
        order.changeStatus(OrderStatus.EXCHANGE_REQUESTED);
        orderRepository.save(order);
    }

    /**
     * 주문 취소 승인 (관리자용)
     */
    @Transactional
    public void approveOrderCancellation(Long orderId, User admin) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (order.getStatus() != OrderStatus.CANCELLATION_REQUESTED) {
            throw new IllegalStateException("취소 요청된 주문만 승인할 수 있습니다.");
        }
        
        // 재고 복원
        restoreStock(order);
        
        // 취소 완료 처리
        order.completeCancellation();
        orderRepository.save(order);
    }

    /**
     * 주문 상태 변경 (관리자 또는 작가용)
     */
    @Transactional
    public void changeOrderStatus(Long orderId, OrderStatusChangeRequestDto requestDto, User user) {
        // N+1 방지를 위해 Fetch Join으로 주문 조회
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 검증: 관리자/루트는 모든 주문, 작가는 자신의 상품 주문만 변경 가능
        if (!user.isAdmin() && !Role.ROOT.equals(user.getRole())) {
            if (!user.isArtist()) {
                throw new ServiceException("403", "주문 상태 변경 권한이 없습니다.");
            }
            
            // 작가인 경우: 자신의 상품인지 확인 (이미 Fetch Join으로 Product 로딩됨)
            boolean hasPermission = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getUser().getId().equals(user.getId()));
            
            if (!hasPermission) {
                throw new ServiceException("403", "자신의 상품 주문만 상태 변경할 수 있습니다.");
            }
        }
        
        OrderStatus oldStatus = order.getStatus();
        order.changeStatus(requestDto.status());
        orderRepository.save(order);
        
        // ✅ 배송 완료 시 작가에게 수익 적립
        if (requestDto.status() == OrderStatus.DELIVERED) {
            creditArtistRevenue(order);
        }
        
        // 알림 발송 - 상태별 처리
        User customer = order.getUser();
        
        switch (requestDto.status()) {
            case SHIPPING:
                // 배송 시작 알림
                notificationService.sendNotification(
                    customer,
                    NotificationType.SHIPPING_STARTED,
                    "상품이 발송되었습니다. 주문번호: " + order.getOrderNumber(),
                    "/mypage/orders/" + order.getId()
                );
                break;
                
            case DELIVERED:
                // 배송 완료 알림
                notificationService.sendNotification(
                    customer,
                    NotificationType.DELIVERY_COMPLETED,
                    "배송이 완료되었습니다. 주문번호: " + order.getOrderNumber(),
                    "/mypage/orders/" + order.getId()
                );
                break;
                
            case REFUND_COMPLETED:
                // 환불 완료 알림
                notificationService.sendNotification(
                    customer,
                    NotificationType.REFUND_COMPLETED,
                    "환불이 완료되었습니다. 주문번호: " + order.getOrderNumber(),
                    "/mypage/orders/" + order.getId()
                );
                break;
                
            default:
                // 다른 상태 변경은 알림 없음
                break;
        }
    }

    /**
     * 주문 배송 완료 시 작가에게 수익 적립
     */
    @Transactional
    public void creditArtistRevenue(Order order) {
        log.info("작가 수익 적립 시작 - 주문ID: {}", order.getId());
        
        // 각 주문 상품별로 작가에게 수익 적립
        order.getOrderItems().forEach(orderItem -> {
            User artist = orderItem.getProduct().getUser();
            
            // 작가의 모리캐시 잔액 조회 또는 생성 (Pessimistic Write Lock - 동시성 제어)
            com.back.domain.payment.moriCash.entity.MoriCashBalance balance = 
                    moriCashBalanceRepository.findByUserWithLock(artist)
                            .orElseGet(() -> {
                                com.back.domain.payment.moriCash.entity.MoriCashBalance newBalance = 
                                        com.back.domain.payment.moriCash.entity.MoriCashBalance.createInitialBalance(artist);
                                return moriCashBalanceRepository.save(newBalance);
                            });
            
            // 수익 계산 (상품 금액 - 수수료)
            int itemTotal = orderItem.getPrice().intValue() * orderItem.getQuantity();
            int commission = itemTotal / 10; // 10% 수수료
            int netAmount = itemTotal - commission;
            
            // 작가 모리캐시 증가
            balance.addSalesRevenue(netAmount);
            moriCashBalanceRepository.save(balance);
            
            log.info("작가 수익 적립 완료 - 작가ID: {}, 상품: {}, 총액: {}, 수수료: {}, 순수익: {}", 
                    artist.getId(), orderItem.getProduct().getName(), itemTotal, commission, netAmount);
        });
    }

    // ==================== Private Methods ====================

    /**
     * 주문상품 생성 (CartType별 분기)
     */
    private List<OrderItem> createOrderItems(List<OrderRequestDto.OrderItemRequestDto> orderItemRequests) {
        return orderItemRequests.stream()
                .map(itemRequest -> {
                    String cartType = itemRequest.cartType();
                    
                    if ("NORMAL".equals(cartType)) {
                        return createNormalOrderItem(itemRequest);
                    } else if ("FUNDING".equals(cartType)) {
                        return createFundingOrderItem(itemRequest);
                    } else {
                        throw new ServiceException("400", "지원하지 않는 장바구니 타입입니다: " + cartType);
                    }
                })
                .toList();
    }

    /**
     * NORMAL 상품 주문 아이템 생성
     */
    private OrderItem createNormalOrderItem(OrderRequestDto.OrderItemRequestDto itemRequest) {
        if (itemRequest.productUuid() == null) {
            throw new ServiceException("400", "NORMAL 상품은 productUuid가 필수입니다.");
        }
        
        // Pessimistic Write Lock으로 상품 조회 (동시성 제어)
        Product product = productRepository.findByProductUuidWithLock(itemRequest.productUuid())
                .orElseThrow(() -> new ServiceException("404", "상품을 찾을 수 없습니다."));
        
        // 재고 검증 및 감소
        int newStock = product.getStock() - itemRequest.quantity();
        if (newStock < 0) {
            throw new ServiceException("400", "재고가 부족합니다. (상품: " + product.getName() + ", 현재 재고: " + product.getStock() + ")");
        }
        product.setStock(newStock);
        
        return OrderItem.createOrderItem(product, itemRequest.quantity(), itemRequest.optionInfo());
    }

    /**
     * FUNDING 상품 주문 아이템 생성
     */
    private OrderItem createFundingOrderItem(OrderRequestDto.OrderItemRequestDto itemRequest) {
        if (itemRequest.fundingId() == null) {
            throw new ServiceException("400", "FUNDING 상품은 fundingId가 필수입니다.");
        }
        
        // 1. Funding 엔티티 조회
        Funding funding = fundingRepository.findById(itemRequest.fundingId())
                .orElseThrow(() -> new ServiceException("404", "펀딩을 찾을 수 없습니다: " + itemRequest.fundingId()));
        
        // 2. 펀딩 상태 검증 (OPEN 상태만 주문 가능)
        if (funding.getStatus() != FundingStatus.OPEN) {
            throw new ServiceException("400", "주문할 수 없는 펀딩 상태입니다: " + funding.getStatus());
        }
        
        // 3. 펀딩 기간 검증 (진행 중인 펀딩만)
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(funding.getStartDate()) || now.isAfter(funding.getEndDate())) {
            throw new ServiceException("400", "펀딩 기간이 아닙니다. 시작일: " + funding.getStartDate() + ", 종료일: " + funding.getEndDate());
        }
        
        // 4. 재고 검증 (Funding 엔티티의 hasStock 메서드 사용)
        if (!funding.hasStock(itemRequest.quantity())) {
            Integer availableStock = funding.getRemainingStock();
            throw new ServiceException("400", "펀딩 재고가 부족합니다. 요청 수량: " + itemRequest.quantity() + 
                    (availableStock != null ? ", 남은 재고: " + availableStock : ", 무제한 재고"));
        }
        
        // 5. 재고 차감 (Funding 엔티티의 decreaseStock 메서드 사용)
        // decreaseStock 내부에서 재고 검증을 다시 수행하므로 안전
        funding.decreaseStock(itemRequest.quantity());
        
        // 6. OrderItem 생성 (Funding용) - BigDecimal 사용
        BigDecimal fundingPrice = itemRequest.fundingPrice() != null 
                ? BigDecimal.valueOf(itemRequest.fundingPrice()) 
                : BigDecimal.valueOf(funding.getPrice());
        
        return OrderItem.createFundingOrderItem(
                funding,
                itemRequest.quantity(),
                itemRequest.optionInfo(),
                fundingPrice
        );
    }

    /**
     * 장바구니에서 제거 (CartType별 분기)
     */
    private void removeFromCart(User user, List<OrderRequestDto.OrderItemRequestDto> orderItems) {
        if (orderItems.isEmpty()) {
            return;
        }
        
        // NORMAL과 FUNDING을 분리해서 처리
        List<UUID> productUuids = orderItems.stream()
                .filter(item -> "NORMAL".equals(item.cartType()))
                .map(OrderRequestDto.OrderItemRequestDto::productUuid)
                .filter(java.util.Objects::nonNull)
                .toList();
        
        List<Long> fundingIds = orderItems.stream()
                .filter(item -> "FUNDING".equals(item.cartType()))
                .map(OrderRequestDto.OrderItemRequestDto::fundingId)
                .filter(java.util.Objects::nonNull)
                .toList();
        
        // NORMAL 상품 삭제
        if (!productUuids.isEmpty()) {
            cartRepository.deleteByUserAndProductUuidIn(user, productUuids);
        }
        
        // FUNDING 상품 삭제
        if (!fundingIds.isEmpty()) {
            cartRepository.deleteByUserAndFundingIdIn(user, fundingIds);
        }
    }

    /**
     * 재고 복원 (주문 취소 시) - NORMAL/FUNDING 분기 처리
     */
    private void restoreStock(Order order) {
        order.getOrderItems().forEach(orderItem -> {
            if (orderItem.getProduct() != null) {
                // NORMAL 상품 재고 복원
                Product product = orderItem.getProduct();
                int restoredStock = product.getStock() + orderItem.getQuantity();
                product.setStock(restoredStock);
                
                // 품절 해제 (품절 상태였다면 판매중으로 변경)
                if (product.getSellingStatus() == SellingStatus.SOLD_OUT) {
                    product.setSellingStatus(SellingStatus.SELLING);
                }
            } else if (orderItem.getFunding() != null) {
                // FUNDING 상품 재고 복원
                Funding funding = orderItem.getFunding();
                funding.increaseStock(orderItem.getQuantity());
            }
        });
    }

    /**
     * 재고 부족 알림 발송
     */
    private void sendLowStockNotifications(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProduct() != null) {
                // NORMAL 상품 처리
                Product product = orderItem.getProduct();
                
                // 품절 처리 (재고가 0이 되면 자동으로 품절 상태로 변경)
                if (product.getStock() == 0) {
                    product.setSellingStatus(SellingStatus.SOLD_OUT);
                }
                
                // 재고 부족 알림 (재고가 5개 이하이고 0개 초과일 때)
                if (product.getStock() <= 5 && product.getStock() > 0) {
                    notificationService.sendNotification(
                        product.getUser(),
                        NotificationType.LOW_STOCK,
                        product.getName() + "의 재고가 부족합니다. (남은 재고: " + product.getStock() + "개)",
                        "/artist/products/" + product.getProductUuid()
                    );
                }
            } else if (orderItem.getFunding() != null) {
                // FUNDING 상품 처리
                Funding funding = orderItem.getFunding();
                Integer currentStock = funding.getStock();
                
                // 재고 부족 알림 (재고가 5개 이하이고 0개 초과일 때)
                if (currentStock != null && currentStock <= 5 && currentStock > 0) {
                    notificationService.sendNotification(
                        funding.getUser(),
                        NotificationType.LOW_STOCK,
                        funding.getTitle() + "의 재고가 부족합니다. (남은 재고: " + currentStock + "개)",
                        "/artist/fundings/" + funding.getId()
                    );
                }
            }
        }
    }

    /**
     * 새로운 주문 알림 발송 (작가에게)
     */
    private void sendNewOrderNotifications(List<OrderItem> orderItems, Order savedOrder) {
        orderItems.stream()
            .map(orderItem -> {
                if (orderItem.getProduct() != null) {
                    return orderItem.getProduct().getUser(); // NORMAL 상품 작가
                } else if (orderItem.getFunding() != null) {
                    return orderItem.getFunding().getUser(); // FUNDING 상품 작가
                }
                return null;
            })
            .filter(java.util.Objects::nonNull)
            .distinct()
            .forEach(artist -> {
                notificationService.sendNotification(
                    artist,
                    NotificationType.NEW_ORDER,
                    "새로운 주문이 들어왔습니다. 주문번호: " + savedOrder.getOrderNumber(),
                    "/artist/orders/" + savedOrder.getId()
                );
            });
    }

    /**
     * 상품 썸네일 이미지 URL 조회
     */
    private String getProductThumbnailUrl(Product product) {
        return product.getImages().stream()
                .filter(image -> "THUMBNAIL".equals(image.getFileType().name()))
                .findFirst()
                .map(image -> image.getFileUrl())
                .orElse(null);
    }

    /**
     * 펀딩 썸네일 이미지 URL 조회
     */
    private String getFundingThumbnailUrl(Funding funding) {
        try {
            // Funding.images는 LAZY 로딩이므로 안전하게 접근
            if (funding.getImages() != null && !funding.getImages().isEmpty()) {
                return funding.getImages().stream()
                        .filter(image -> "THUMBNAIL".equals(image.getFileType().name()))
                        .findFirst()
                        .map(image -> image.getFileUrl())
                        .orElse(funding.getImageUrl()); // 썸네일이 없으면 메인 이미지 사용
            }
        } catch (Exception e) {
            // LAZY 로딩 실패 시 메인 이미지 사용
            log.warn("Funding 이미지 로딩 실패, 메인 이미지 사용: fundingId={}", funding.getId());
        }
        return funding.getImageUrl(); // 기본 이미지 사용
    }

    /**
     * OrderResponseDto 변환
     */
    private OrderResponseDto convertToOrderResponseDto(Order order) {
        List<OrderResponseDto.OrderItemResponseDto> orderItemDtos = order.getOrderItems().stream()
                .map(item -> {
                    String name;
                    String imageUrl;
                    UUID productUuid = null;
                    BigDecimal price = item.getPrice();

                    if (item.getProduct() != null) {
                        // NORMAL 상품
                        Product product = item.getProduct();
                        productUuid = product.getProductUuid();
                        name = product.getName();
                        imageUrl = getProductThumbnailUrl(product);

                    } else if (item.getFunding() != null) {
                        // FUNDING 상품
                        Funding funding = item.getFunding();
                        name = funding.getTitle();
                        imageUrl = getFundingThumbnailUrl(funding); // FUNDING 썸네일 이미지
                    } else {
                        name = "알 수 없는 상품";
                        imageUrl = null;
                    }

                    return new OrderResponseDto.OrderItemResponseDto(
                            item.getId(),
                            productUuid,
                            name,
                            imageUrl,
                            item.getQuantity(),
                            price,
                            item.getTotalPrice(),
                            item.getOptionInfo()
                    );
                })
                .toList();
        
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalQuantity(),
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getFinalAmount(),
                order.getShippingAddress1(),
                order.getShippingAddress2(),
                order.getShippingZip(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getDeliveryRequest(),
                order.getPaymentMethod(),
                order.getOrderDate(),
                orderItemDtos
        );
    }

}
