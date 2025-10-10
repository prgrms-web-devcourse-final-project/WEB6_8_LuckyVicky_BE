package com.back.domain.payment.moriCash.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.payment.moriCash.dto.request.MoriCashPaymentRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.entity.MoriCashPayment;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.payment.moriCash.entity.TransactionType;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.payment.moriCash.repository.MoriCashPaymentRepository;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoriCashPaymentServiceTest {

    @Mock
    private MoriCashPaymentRepository moriCashPaymentRepository;

    @Mock
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private MoriCashPaymentService moriCashPaymentService;

    private User user;
    private Order order;
    private MoriCashPaymentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = createTestUser();
        order = createTestOrder();
        requestDto = createTestRequestDto();
    }

    @Test
    @DisplayName("모리캐시 결제 - 성공")
    void createPayment_Success() {
        // Given
        MoriCashBalance balance = createTestBalance(15000);
        MoriCashPayment savedPayment = createTestPayment();
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashPaymentRepository.save(any(MoriCashPayment.class))).thenReturn(savedPayment);
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        MoriCashPaymentResponseDto result = moriCashPaymentService.createPayment(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPrice()).isEqualTo(10000);
        assertThat(result.getUsedMoriCash()).isEqualTo(8000);
        assertThat(result.getStatus()).isEqualTo(MoriCashPaymentStatus.COMPLETED);

        verify(orderRepository).findById(1L);
        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashPaymentRepository).save(any(MoriCashPayment.class));
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
    }

    @Test
    @DisplayName("모리캐시 결제 - 주문을 찾을 수 없는 경우")
    void createPayment_OrderNotFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moriCashPaymentService.createPayment(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("모리캐시 결제 - 본인 주문이 아닌 경우")
    void createPayment_NotOwnOrder() {
        // Given
        User otherUser = createOtherUser();
        Order otherOrder = Order.builder()
                .user(otherUser)
                .orderNumber("ORD999999")
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(1)
                .totalAmount(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("3000"))
                .finalAmount(new BigDecimal("13000"))
                .orderDate(LocalDateTime.now())
                .build();
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(otherOrder));

        // When & Then
        assertThatThrownBy(() -> moriCashPaymentService.createPayment(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 주문만 결제할 수 있습니다.");
    }

    @Test
    @DisplayName("모리캐시 결제 - 잔액 부족")
    void createPayment_InsufficientBalance() {
        // Given
        MoriCashBalance balance = createTestBalance(5000); // 요청 금액보다 적음
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));

        // When & Then
        assertThatThrownBy(() -> moriCashPaymentService.createPayment(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모리캐시 잔액이 부족합니다.");
    }

    @Test
    @DisplayName("모리캐시 결제 - 잔액이 없는 경우 예외 발생")
    void createPayment_CreateNewBalance() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moriCashPaymentService.createPayment(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모리캐시 잔액이 부족합니다.");
    }

    @Test
    @DisplayName("모리캐시 결제 취소 - 성공")
    void cancelPayment_Success() {
        // Given
        Long paymentId = 1L;
        MoriCashPayment payment = createTestPayment();
        MoriCashBalance balance = createTestBalance(15000);
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        moriCashPaymentService.cancelPayment(paymentId, user);

        // Then
        verify(moriCashPaymentRepository).findById(paymentId);
        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
        
        assertThat(payment.getStatus()).isEqualTo(MoriCashPaymentStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLATION_REQUESTED);
    }

    @Test
    @DisplayName("모리캐시 결제 취소 - 본인 결제가 아닌 경우")
    void cancelPayment_NotOwnPayment() {
        // Given
        Long paymentId = 1L;
        User otherUser = createOtherUser();
        MoriCashPayment payment = MoriCashPayment.builder()
                .order(order)
                .user(otherUser)
                .totalPrice(10000)
                .usedMoriCash(8000)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(7000)
                .build();
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashPaymentService.cancelPayment(paymentId, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 결제만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("모리캐시 결제 취소 - 완료되지 않은 결제")
    void cancelPayment_NotCompletedPayment() {
        // Given
        Long paymentId = 1L;
        MoriCashPayment payment = MoriCashPayment.builder()
                .order(order)
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(8000)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(7000)
                .build();
        // PENDING 상태는 Builder에서 기본값으로 설정됨
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashPaymentService.cancelPayment(paymentId, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("완료된 결제만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("모리캐시 결제 상세 조회 - 성공")
    void getPayment_Success() {
        // Given
        Long paymentId = 1L;
        MoriCashPayment payment = createTestPayment(); // 이미 ID = 1L이 설정되어 있음
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        MoriCashPaymentResponseDto result = moriCashPaymentService.getPayment(paymentId, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        verify(moriCashPaymentRepository).findById(paymentId);
    }

    // Helper Methods
    private User createTestUser() {
        User user = User.createLocalUser(
                "test@example.com",
                "password123",
                "테스트유저",
                "010-1234-5678"
        );
        try {
            Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private User createOtherUser() {
        User user = User.createLocalUser(
                "other@example.com",
                "password123",
                "다른유저",
                "010-9999-9999"
        );
        try {
            Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 2L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private Order createTestOrder() {
        Order order = Order.builder()
                .user(user)
                .orderNumber("ORD123456")
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(1)
                .totalAmount(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("3000"))
                .finalAmount(new BigDecimal("13000"))
                .orderDate(LocalDateTime.now())
                .build();
        try {
            Field idField = order.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    private MoriCashPaymentRequestDto createTestRequestDto() {
        return MoriCashPaymentRequestDto.builder()
                .orderId(1L)
                .totalPrice(10000)
                .usedMoriCash(8000)
                .build();
    }

    private MoriCashPayment createTestPayment() {
        MoriCashPayment payment = MoriCashPayment.builder()
                .order(order)
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(8000)
                .transactionType(TransactionType.PURCHASE)
                .status(MoriCashPaymentStatus.COMPLETED)
                .description("상품 구매")
                .balanceAfter(7000)
                .build();
        try {
            Field idField = payment.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return payment;
    }

    private MoriCashBalance createTestBalance(int availableBalance) {
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(availableBalance)
                .availableBalance(availableBalance)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        return balance;
    }

}
