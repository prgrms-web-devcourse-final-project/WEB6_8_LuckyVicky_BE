package com.back.domain.order.exchange.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.order.exchange.dto.request.ExchangeRequestDto;
import com.back.domain.order.exchange.dto.response.ExchangeResponseDto;
import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.repository.ExchangeItemRepository;
import com.back.domain.order.exchange.repository.ExchangeRepository;
import com.back.domain.order.exchange.util.ExchangeConverter;
import com.back.domain.product.product.entity.Product;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeService 테스트")
class ExchangeServiceTest {

    @Mock
    private ExchangeRepository exchangeRepository;
    
    @Mock
    private ExchangeItemRepository exchangeItemRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private ExchangeConverter exchangeConverter;

    @InjectMocks
    private ExchangeService exchangeService;

    private User user;
    private Order order;
    private OrderItem orderItem;
    private Product product;
    private Exchange exchange;
    private ExchangeRequestDto requestDto;

    @BeforeEach
    void setUp() throws Exception {
        // User 설정 (팩토리 메서드 사용 + 리플렉션으로 ID 설정)
        user = User.createLocalUser("test@example.com", "password", "테스트유저", "010-1234-5678");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);

        // Product 설정 (Builder 사용)
        product = Product.builder()
                .name("테스트 상품")
                .price(10000)
                .build();

        // OrderItem 설정 (Builder 사용)
        orderItem = OrderItem.builder()
                .product(product)
                .quantity(2)
                .price(java.math.BigDecimal.valueOf(10000))
                .build();

        // Order 설정 (Builder 사용)
        order = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(1)) // 1일 전 주문
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();

        // Exchange 설정 (팩토리 메서드 사용)
        exchange = Exchange.createExchange(
                order,
                user,
                "상품 불량",
                "세부 사유",
                Exchange.ExchangeMethod.PICKUP,
                null,
                "새 주소",
                "상세 주소",
                "12345",
                "새 수령인",
                "010-1234-5678"
        );

        // RequestDto 설정
        requestDto = new ExchangeRequestDto(
                1L,
                List.of(1L),
                "상품 불량",
                "세부 사유",
                Exchange.ExchangeMethod.PICKUP,
                List.of("file1.jpg", "file2.jpg"),
                "새 주소",
                "상세 주소",
                "12345",
                "새 수령인",
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("교환 신청 성공")
    void createExchange_Success() {
        // given
        Exchange savedExchange = Exchange.createExchange(
                order,
                user,
                "상품 불량",
                "세부 사유",
                Exchange.ExchangeMethod.PICKUP,
                null,
                "새 주소",
                "상세 주소",
                "12345",
                "새 수령인",
                "010-1234-5678"
        );
        // 리플렉션으로 ID 설정
        try {
            java.lang.reflect.Field idField = savedExchange.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedExchange, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderItemRepository.findAllById(List.of(1L))).willReturn(List.of(orderItem));
        given(exchangeRepository.save(any(Exchange.class))).willReturn(savedExchange);
        given(exchangeRepository.findByIdWithItems(1L)).willReturn(Optional.of(savedExchange));
        given(exchangeConverter.toResponseDto(any(Exchange.class), anyList())).willReturn(mock(ExchangeResponseDto.class));

        // when
        ExchangeResponseDto result = exchangeService.createExchange(requestDto, user);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderItemRepository).findAllById(List.of(1L));
        verify(exchangeRepository).save(any(Exchange.class));
        verify(exchangeItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 교환 신청 시 예외 발생")
    void createExchange_OrderNotFound_ThrowsException() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> exchangeService.createExchange(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("권한이 없는 사용자의 교환 신청 시 예외 발생")
    void createExchange_UnauthorizedUser_ThrowsException() throws Exception {
        // given
        User otherUser = User.createLocalUser("other@example.com", "password", "다른유저", "010-9876-5432");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = otherUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);
        
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> exchangeService.createExchange(requestDto, otherUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 주문에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("배송완료되지 않은 주문의 교환 신청 시 예외 발생")
    void createExchange_OrderNotDelivered_ThrowsException() {
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
        given(orderRepository.findById(1L)).willReturn(Optional.of(preparingOrder));
        given(orderItemRepository.findAllById(List.of(1L))).willReturn(List.of(orderItem));

        // when & then
        assertThatThrownBy(() -> exchangeService.createExchange(requestDto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료된 주문만 교환 신청이 가능합니다.");
    }

    @Test
    @DisplayName("7일 초과된 주문의 교환 신청 시 예외 발생")
    void createExchange_ExceededDeadline_ThrowsException() {
        // given
        Order expiredOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(10))
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(expiredOrder));
        given(orderItemRepository.findAllById(List.of(1L))).willReturn(List.of(orderItem));

        // when & then
        assertThatThrownBy(() -> exchangeService.createExchange(requestDto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료 후 7일 이내에만 교환 신청이 가능합니다.");
    }

    @Test
    @DisplayName("교환 상세 조회 성공")
    void getExchange_Success() {
        // given
        given(exchangeRepository.findByIdWithItems(1L)).willReturn(Optional.of(exchange));
        given(exchangeConverter.toResponseDto(any(Exchange.class), anyList())).willReturn(mock(ExchangeResponseDto.class));

        // when
        ExchangeResponseDto result = exchangeService.getItem(1L, user);

        // then
        assertThat(result).isNotNull();
        verify(exchangeRepository).findByIdWithItems(1L);
        verify(exchangeConverter).toResponseDto(any(Exchange.class), anyList());
    }

    @Test
    @DisplayName("존재하지 않는 교환 조회 시 예외 발생")
    void getExchange_NotFound_ThrowsException() {
        // given
        given(exchangeRepository.findByIdWithItems(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> exchangeService.getItem(1L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("교환 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("권한이 없는 사용자의 교환 조회 시 예외 발생")
    void getExchange_UnauthorizedUser_ThrowsException() throws Exception {
        // given
        User otherUser = User.createLocalUser("other@example.com", "password", "다른유저", "010-9876-5432");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = otherUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);
        
        given(exchangeRepository.findByIdWithItems(1L)).willReturn(Optional.of(exchange));

        // when & then
        assertThatThrownBy(() -> exchangeService.getItem(1L, otherUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 교환에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("사용자별 교환 목록 조회 성공")
    void getExchangesByUser_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(exchangeRepository.findByUserWithItems(user)).willReturn(List.of(exchange));
        given(exchangeConverter.toResponseDto(any(Exchange.class), anyList())).willReturn(mock(ExchangeResponseDto.class));

        // when
        Page<ExchangeResponseDto> result = exchangeService.getItemsByUser(user, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(exchangeRepository).findByUserWithItems(user);
    }

    @Test
    @DisplayName("교환 승인 성공")
    void approveExchange_Success() throws Exception {
        // given
        User admin = User.createLocalUser("admin@example.com", "password", "관리자", "010-1111-2222");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = admin.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(admin, 2L);
        
        admin.becomeArtist(); // ADMIN 역할로 변경
        given(exchangeRepository.findById(1L)).willReturn(Optional.of(exchange));
        given(exchangeRepository.findByIdWithItems(1L)).willReturn(Optional.of(exchange));
        given(exchangeConverter.toResponseDto(any(Exchange.class), anyList())).willReturn(mock(ExchangeResponseDto.class));

        // when
        ExchangeResponseDto result = exchangeService.approveItem(1L, admin);

        // then
        assertThat(result).isNotNull();
        verify(exchangeRepository).findById(1L);
        verify(exchangeRepository).findByIdWithItems(1L);
        assertThat(exchange.getStatus()).isEqualTo(Exchange.ExchangeStatus.COMPLETED);
    }

    @Test
    @DisplayName("이미 승인된 교환 재승인 시 예외 발생")
    void approveExchange_AlreadyApproved_ThrowsException() throws Exception {
        // given
        User admin = User.createLocalUser("admin@example.com", "password", "관리자", "010-1111-2222");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = admin.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(admin, 2L);
        
        admin.becomeArtist(); // ADMIN 역할로 변경
        exchange.approve(); // 이미 승인된 상태로 변경
        given(exchangeRepository.findById(1L)).willReturn(Optional.of(exchange));

        // when & then
        assertThatThrownBy(() -> exchangeService.approveItem(1L, admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("승인 대기 중인 교환만 승인할 수 있습니다.");
    }
}
