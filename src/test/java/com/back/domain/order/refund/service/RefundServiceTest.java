package com.back.domain.order.refund.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.orderItem.repository.OrderItemRepository;
import com.back.domain.order.refund.dto.request.RefundRequestDto;
import com.back.domain.order.refund.dto.response.RefundResponseDto;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.repository.RefundItemRepository;
import com.back.domain.order.refund.repository.RefundRepository;
import com.back.domain.order.refund.util.RefundConverter;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundService 테스트")
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;
    
    @Mock
    private RefundItemRepository refundItemRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private RefundConverter refundConverter;

    @InjectMocks
    private RefundService refundService;

    private User user;
    private Order order;
    private OrderItem orderItem;
    private Product product;
    private Refund refund;
    private RefundRequestDto requestDto;

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
                .price(BigDecimal.valueOf(10000))
                .build();

        // Order 설정 (Builder 사용)
        order = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(1)) // 1일 전 주문
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(20000))
                .shippingFee(BigDecimal.valueOf(3000))
                .finalAmount(BigDecimal.valueOf(23000))
                .build();

        // Refund 설정 (팩토리 메서드 사용)
        refund = Refund.createRefund(
                order,
                user,
                com.back.domain.order.refund.entity.RefundReasonType.DEFECTIVE,
                "상품 불량",
                "세부 사유",
                BigDecimal.valueOf(20000),
                Refund.RefundMethod.ORIGINAL_PAYMENT,
                null
        );

        // RequestDto 설정
        requestDto = new RefundRequestDto(
                1L,
                List.of(1L),
                com.back.domain.order.refund.entity.RefundReasonType.DEFECTIVE,
                "상품 불량",
                "세부 사유",
                BigDecimal.valueOf(20000),
                Refund.RefundMethod.ORIGINAL_PAYMENT,
                List.of("file1.jpg", "file2.jpg")
        );
    }

    @Test
    @DisplayName("환불 신청 성공")
    void createRefund_Success() {
        // given
        Refund savedRefund = Refund.createRefund(
                order,
                user,
                com.back.domain.order.refund.entity.RefundReasonType.DEFECTIVE,
                "상품 불량",
                "세부 사유",
                BigDecimal.valueOf(20000),
                Refund.RefundMethod.ORIGINAL_PAYMENT,
                null
        );
        // 리플렉션으로 ID 설정
        try {
            java.lang.reflect.Field idField = savedRefund.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedRefund, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderItemRepository.findAllById(List.of(1L))).willReturn(List.of(orderItem));
        given(refundRepository.save(any(Refund.class))).willReturn(savedRefund);
        given(refundRepository.findByIdWithItems(1L)).willReturn(Optional.of(savedRefund));
        given(refundConverter.toResponseDto(any(Refund.class), anyList())).willReturn(mock(RefundResponseDto.class));

        // when
        RefundResponseDto result = refundService.createRefund(requestDto, user);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderItemRepository).findAllById(List.of(1L));
        verify(refundRepository).save(any(Refund.class));
        verify(refundItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 환불 신청 시 예외 발생")
    void createRefund_OrderNotFound_ThrowsException() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refundService.createRefund(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("권한이 없는 사용자의 환불 신청 시 예외 발생")
    void createRefund_UnauthorizedUser_ThrowsException() throws Exception {
        // given
        User otherUser = User.createLocalUser("other@example.com", "password", "다른유저", "010-9876-5432");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = otherUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);
        
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> refundService.createRefund(requestDto, otherUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 주문에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("배송완료되지 않은 주문의 환불 신청 시 예외 발생")
    void createRefund_OrderNotDelivered_ThrowsException() {
        // given
        Order preparingOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.PREPARING_SHIPMENT)
                .orderDate(LocalDateTime.now().minusDays(1))
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(20000))
                .shippingFee(BigDecimal.valueOf(3000))
                .finalAmount(BigDecimal.valueOf(23000))
                .build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(preparingOrder));
        given(orderItemRepository.findAllById(List.of(1L))).willReturn(List.of(orderItem));

        // when & then
        assertThatThrownBy(() -> refundService.createRefund(requestDto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료된 주문만 환불 신청이 가능합니다.");
    }

    @Test
    @DisplayName("7일 초과된 주문의 환불 신청 시 예외 발생")
    void createRefund_ExceededDeadline_ThrowsException() {
        // given
        Order expiredOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDateTime.now().minusDays(10))
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(20000))
                .shippingFee(BigDecimal.valueOf(3000))
                .finalAmount(BigDecimal.valueOf(23000))
                .build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(expiredOrder));
        given(orderItemRepository.findAllById(List.of(1L))).willReturn(List.of(orderItem));

        // when & then
        assertThatThrownBy(() -> refundService.createRefund(requestDto, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송완료 후 7일 이내에만 환불 신청이 가능합니다.");
    }

    @Test
    @DisplayName("환불 상세 조회 성공")
    void getRefund_Success() {
        // given
        given(refundRepository.findByIdWithItems(1L)).willReturn(Optional.of(refund));
        given(refundConverter.toResponseDto(any(Refund.class), anyList())).willReturn(mock(RefundResponseDto.class));

        // when
        RefundResponseDto result = refundService.getItem(1L, user);

        // then
        assertThat(result).isNotNull();
        verify(refundRepository).findByIdWithItems(1L);
        verify(refundConverter).toResponseDto(any(Refund.class), anyList());
    }

    @Test
    @DisplayName("존재하지 않는 환불 조회 시 예외 발생")
    void getRefund_NotFound_ThrowsException() {
        // given
        given(refundRepository.findByIdWithItems(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refundService.getItem(1L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환불 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("권한이 없는 사용자의 환불 조회 시 예외 발생")
    void getRefund_UnauthorizedUser_ThrowsException() throws Exception {
        // given
        User otherUser = User.createLocalUser("other@example.com", "password", "다른유저", "010-9876-5432");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = otherUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);
        
        given(refundRepository.findByIdWithItems(1L)).willReturn(Optional.of(refund));

        // when & then
        assertThatThrownBy(() -> refundService.getItem(1L, otherUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 환불에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("사용자별 환불 목록 조회 성공")
    void getRefundsByUser_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(refundRepository.findByUserWithItems(user)).willReturn(List.of(refund));
        given(refundConverter.toResponseDto(any(Refund.class), anyList())).willReturn(mock(RefundResponseDto.class));

        // when
        Page<RefundResponseDto> result = refundService.getItemsByUser(user, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(refundRepository).findByUserWithItems(user);
    }

    @Test
    @DisplayName("환불 승인 성공")
    void approveRefund_Success() throws Exception {
        // given
        User admin = User.createLocalUser("admin@example.com", "password", "관리자", "010-1111-2222");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = admin.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(admin, 2L);
        
        admin.becomeArtist(); // ADMIN 역할로 변경
        given(refundRepository.findByIdWithItems(1L)).willReturn(Optional.of(refund));
        given(refundConverter.toResponseDto(any(Refund.class), anyList())).willReturn(mock(RefundResponseDto.class));

        // when
        RefundResponseDto result = refundService.approveItem(1L, admin);

        // then
        assertThat(result).isNotNull();
        verify(refundRepository).findByIdWithItems(1L);
        assertThat(refund.getStatus()).isEqualTo(Refund.RefundStatus.COMPLETED);
    }

    @Test
    @DisplayName("이미 승인된 환불 재승인 시 예외 발생")
    void approveRefund_AlreadyApproved_ThrowsException() throws Exception {
        // given
        User admin = User.createLocalUser("admin@example.com", "password", "관리자", "010-1111-2222");
        // 리플렉션을 사용해서 ID 설정
        java.lang.reflect.Field idField = admin.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(admin, 2L);
        
        admin.becomeArtist(); // ADMIN 역할로 변경
        refund.approve(); // 이미 승인된 상태로 변경
        given(refundRepository.findByIdWithItems(1L)).willReturn(Optional.of(refund));

        // when & then
        assertThatThrownBy(() -> refundService.approveItem(1L, admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("승인 대기 중인 환불만 승인할 수 있습니다.");
    }
}
