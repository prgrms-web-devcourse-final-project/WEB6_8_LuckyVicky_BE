package com.back.domain.order.order.service;

import com.back.domain.cart.repository.CartRepository;
import com.back.domain.order.order.dto.request.OrderCancelRequestDto;
import com.back.domain.order.order.dto.request.OrderExchangeRequestDto;
import com.back.domain.order.order.dto.request.OrderRefundRequestDto;
import com.back.domain.order.order.dto.request.OrderRequestDto;
import com.back.domain.order.order.dto.response.OrderResponseDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;

    @InjectMocks private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Product product = createProduct(1L, productUuid);
        OrderRequestDto requestDto = createOrderRequestDto(productUuid);

        given(productRepository.findByProductUuid(productUuid)).willReturn(Optional.of(product));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto result = orderService.createOrder(user, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        verify(productRepository).findByProductUuid(productUuid);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).deleteByUserAndProductUuidIn(eq(user), anyList());
    }

    @Test
    @DisplayName("주문 생성 실패 - 상품 없음")
    void createOrder_ProductNotFound() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        OrderRequestDto requestDto = createOrderRequestDto(productUuid);
        given(productRepository.findByProductUuid(productUuid)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(user, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 목록 조회 성공")
    void getOrderList_Success() {
        User user = createUser(1L);
        Pageable pageable = PageRequest.of(0, 10);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        given(orderRepository.findByUserOrderByOrderDateDesc(user, pageable)).willReturn(orderPage);

        Page<OrderResponseDto> result = orderService.getOrderList(user, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).orderId()).isNotNull();
        assertThat(result.getContent().get(0).orderItems()).isNotEmpty();
        verify(orderRepository).findByUserOrderByOrderDateDesc(user, pageable);
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_Success() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        ReflectionTestUtils.setField(order, "id", 10L);
        given(orderRepository.findByIdWithOrderItems(10L)).willReturn(Optional.of(order));

        OrderResponseDto result = orderService.getOrderDetail(10L, user);

        assertThat(result.orderId()).isEqualTo(10L);
        assertThat(result.orderItems()).isNotEmpty();
        verify(orderRepository).findByIdWithOrderItems(10L);
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 주문 없음")
    void getOrderDetail_NotFound() {
        User user = createUser(1L);
        given(orderRepository.findByIdWithOrderItems(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderDetail(99L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        ReflectionTestUtils.setField(order, "id", 1L);
        given(orderRepository.findByIdWithOrderItems(1L)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(1L, user, new OrderCancelRequestDto("단순 변심", List.of(1L)));

        verify(orderRepository).findByIdWithOrderItems(1L);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("주문 취소 실패 - 배송 중/완료")
    void cancelOrder_Forbidden() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        order.changeStatus(OrderStatus.SHIPPING);
        given(orderRepository.findByIdWithOrderItems(1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, user, new OrderCancelRequestDto("사유", List.of(1L))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송준비중으로 상태가 변경된 이후에는 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("환불 신청 성공")
    void requestRefund_Success() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        order.changeStatus(OrderStatus.DELIVERED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.requestRefund(1L, user, new OrderRefundRequestDto("불량", List.of(1L)));

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("환불 신청 실패 - 배송완료 아님")
    void requestRefund_NotDelivered() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestRefund(1L, user, new OrderRefundRequestDto("불량", List.of(1L))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료된 주문만 환불 신청이 가능합니다.");
    }

    @Test
    @DisplayName("교환 신청 성공")
    void requestExchange_Success() {
        User user = createUser(1L);
        UUID productUuid = UUID.randomUUID();
        Order order = createOrderWithItem(user, createProduct(1L, productUuid));
        order.changeStatus(OrderStatus.DELIVERED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.requestExchange(1L, user, new OrderExchangeRequestDto("사이즈", List.of(1L)));

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    // ==================== Helpers ====================

    private User createUser(Long id) {
        User user = org.mockito.Mockito.mock(User.class);
        lenient().when(user.getId()).thenReturn(id);
        return user;
    }

    private Product createProduct(Long id, UUID productUuid) {
        Product product = Product.builder()
                .name("테스트상품")
                .price(10000)
                .discountRate(10)
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(0)
                // .deliveryType(Product.DeliveryType.PAID)
                .stock(100)
                .description("desc")
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("MODEL")
                .certification(false)
                .origin("KR")
                .material("PAPER")
                .size("M")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .productUuid(productUuid)
                .build();
        // initialize images list to avoid NPE in thumbnail resolver
        java.util.ArrayList<com.back.domain.product.product.entity.ProductImage> images = new java.util.ArrayList<>();
        ReflectionTestUtils.setField(product, "images", images);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Order createOrderWithItem(User user, Product product) {
        OrderItem orderItem = OrderItem.createOrderItem(product, 2, "옵션");
        Order order = Order.createOrder(
                user,
                List.of(orderItem),
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "홍길동",
                "010-1234-5678",
                "문 앞에",
                PaymentMethod.CARD
        );
        ReflectionTestUtils.setField(order, "id", 1L);
        return order;
    }

    private OrderRequestDto createOrderRequestDto(UUID productUuid) {
        return new OrderRequestDto(
                List.of(new OrderRequestDto.OrderItemRequestDto(productUuid, 2, "옵션")),
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "홍길동",
                "010-1234-5678",
                "문 앞에",
                PaymentMethod.CARD
        );
    }
}
