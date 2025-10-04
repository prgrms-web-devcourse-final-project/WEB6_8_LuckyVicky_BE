package com.back.domain.order.common.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseOrderActionService 공통 로직 테스트")
class BaseOrderActionServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;

    private TestOrderActionService service;
    private User user;
    private Order order;
    private OrderItem orderItem;

    // 테스트용 구현체
    private static class TestOrderActionService extends BaseOrderActionService<Object, Object, Object> {
        public TestOrderActionService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
            super(orderRepository, orderItemRepository);
        }

        @Override
        public org.springframework.data.domain.Page<Object> getItemsByUser(User user, org.springframework.data.domain.Pageable pageable) {
            return org.springframework.data.domain.Page.empty();
        }

        @Override
        public Object getItem(Long itemId, User user) {
            return null;
        }

        @Override
        public Object approveItem(Long itemId, User admin) {
            return null;
        }

        // 테스트를 위해 부모 클래스의 protected 메서드를 public으로 노출
        public Order validateOrderAndOwnership(Long orderId, User user) {
            return super.validateOrderAndOwnership(orderId, user);
        }

        public List<OrderItem> validateOrderItems(List<Long> orderItemIds) {
            return super.validateOrderItems(orderItemIds);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        service = new TestOrderActionService(orderRepository, orderItemRepository);
        
        user = User.createLocalUser("test@example.com", "password", "테스트유저", "010-1234-5678");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);

        order = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(1))
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();
        
        // Order에도 ID 설정
        java.lang.reflect.Field orderIdField = order.getClass().getSuperclass().getDeclaredField("id");
        orderIdField.setAccessible(true);
        orderIdField.set(order, 1L);

        Product product = Product.builder()
                .name("테스트 상품")
                .build();
        
        // Product에도 ID 설정
        java.lang.reflect.Field productIdField = product.getClass().getSuperclass().getDeclaredField("id");
        productIdField.setAccessible(true);
        productIdField.set(product, 1L);

        orderItem = OrderItem.builder()
                .product(product)
                .quantity(2)
                .price(java.math.BigDecimal.valueOf(10000))
                .build();
        
        // OrderItem에도 ID 설정
        java.lang.reflect.Field orderItemIdField = orderItem.getClass().getSuperclass().getDeclaredField("id");
        orderItemIdField.setAccessible(true);
        orderItemIdField.set(orderItem, 1L);
    }

    @Test
    @DisplayName("주문 조회 및 권한 체크 성공")
    void validateOrderAndOwnership_Success() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        Order result = service.validateOrderAndOwnership(1L, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
    void validateOrderAndOwnership_OrderNotFound_ThrowsException() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.validateOrderAndOwnership(1L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("권한이 없는 사용자로 주문 조회 시 예외 발생")
    void validateOrderAndOwnership_UnauthorizedUser_ThrowsException() throws Exception {
        // given
        User otherUser = User.createLocalUser("other@example.com", "password", "다른유저", "010-9876-5432");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = otherUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);
        
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> service.validateOrderAndOwnership(1L, otherUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 주문에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("주문상품 조회 및 검증 성공")
    void validateOrderItems_Success() {
        // given
        List<Long> orderItemIds = List.of(1L, 2L);
        List<OrderItem> orderItems = List.of(orderItem);
        given(orderItemRepository.findAllById(orderItemIds)).willReturn(orderItems);

        // when
        List<OrderItem> result = service.validateOrderItems(orderItemIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(orderItemRepository).findAllById(orderItemIds);
    }

    @Test
    @DisplayName("존재하지 않는 주문상품 조회 시 예외 발생")
    void validateOrderItems_NotFound_ThrowsException() {
        // given
        List<Long> orderItemIds = List.of(999L);
        given(orderItemRepository.findAllById(orderItemIds)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> service.validateOrderItems(orderItemIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("환불 가능 여부 검증 성공")
    void validateRefundEligibility_Success() {
        // given
        Order validOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(1)) // 1일 전
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // when & then
        assertThatCode(() -> validOrder.validateRefundEligibility())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배송완료되지 않은 주문의 환불 가능 여부 검증 시 예외 발생")
    void validateRefundEligibility_OrderNotDelivered_ThrowsException() {
        // given
        Order preparingOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.PREPARING_SHIPMENT)
                .orderDate(LocalDateTime.now().minusDays(1))
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // when & then
        assertThatThrownBy(() -> preparingOrder.validateRefundEligibility())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료된 주문만 환불 신청이 가능합니다.");
    }

    @Test
    @DisplayName("7일 초과된 주문의 환불 가능 여부 검증 시 예외 발생")
    void validateRefundEligibility_ExceededDeadline_ThrowsException() {
        // given
        Order expiredOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(10)) // 10일 전
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // when & then
        assertThatThrownBy(() -> expiredOrder.validateRefundEligibility())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료 후 7일 이내에만 환불 신청이 가능합니다.");
    }

    @Test
    @DisplayName("교환 가능 여부 검증 성공")
    void validateExchangeEligibility_Success() {
        // given
        Order validOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(1)) // 1일 전
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // when & then
        assertThatCode(() -> validOrder.validateExchangeEligibility())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배송완료되지 않은 주문의 교환 가능 여부 검증 시 예외 발생")
    void validateExchangeEligibility_OrderNotDelivered_ThrowsException() {
        // given
        Order preparingOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.PREPARING_SHIPMENT)
                .orderDate(LocalDateTime.now().minusDays(1))
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // when & then
        assertThatThrownBy(() -> preparingOrder.validateExchangeEligibility())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료된 주문만 교환 신청이 가능합니다.");
    }

    @Test
    @DisplayName("7일 초과된 주문의 교환 가능 여부 검증 시 예외 발생")
    void validateExchangeEligibility_ExceededDeadline_ThrowsException() {
        // given
        Order expiredOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(10)) // 10일 전
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // when & then
        assertThatThrownBy(() -> expiredOrder.validateExchangeEligibility())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료 후 7일 이내에만 교환 신청이 가능합니다.");
    }
}
