package com.back.domain.payment.moriCash.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.payment.moriCash.dto.request.MoriCashRefundRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashRefundResponseDto;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoriCashRefundServiceTest {

    @Mock
    private MoriCashPaymentRepository moriCashPaymentRepository;

    @Mock
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @InjectMocks
    private MoriCashRefundService moriCashRefundService;

    private User user;
    private MoriCashRefundRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = createTestUser();
        requestDto = createTestRequestDto();
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 성공")
    void processRefund_Success() {
        // Given
        MoriCashPayment payment = createTestPayment();
        MoriCashBalance balance = createTestBalance(10000);
        
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        MoriCashRefundResponseDto result = moriCashRefundService.processRefund(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRefundPrice()).isEqualTo(5000);
        assertThat(result.getBalanceAfter()).isEqualTo(15000); // 10000 + 5000

        verify(moriCashPaymentRepository).findById(1L);
        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 결제를 찾을 수 없는 경우")
    void processRefund_PaymentNotFound() {
        // Given
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.processRefund(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 본인 결제가 아닌 경우")
    void processRefund_NotOwnPayment() {
        // Given
        User otherUser = createOtherUser();
        MoriCashPayment payment = MoriCashPayment.builder()
                .user(otherUser)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .build();
        
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.processRefund(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 결제만 환불할 수 있습니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 완료되지 않은 결제")
    void processRefund_NotCompletedPayment() {
        // Given
        Order testOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123")
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(1)
                .totalAmount(new java.math.BigDecimal("10000"))
                .shippingFee(new java.math.BigDecimal("0"))
                .finalAmount(new java.math.BigDecimal("10000"))
                .build();
        
        MoriCashPayment payment = MoriCashPayment.builder()
                .order(testOrder)
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .status(MoriCashPaymentStatus.PENDING) // PENDING 상태
                .description("상품 구매")
                .balanceAfter(0)
                .build();
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.processRefund(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("완료된 결제만 환불할 수 있습니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 이미 환불된 결제")
    void processRefund_AlreadyRefunded() {
        // Given
        MoriCashPayment payment = MoriCashPayment.builder()
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .refundPrice(3000) // 이미 환불됨
                .build();
        
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.processRefund(requestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("완료된 결제만 환불할 수 있습니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 환불 금액이 올바르지 않은 경우")
    void processRefund_InvalidRefundAmount() {
        // Given
        MoriCashPayment payment = createTestPayment();
        MoriCashRefundRequestDto invalidRequestDto = MoriCashRefundRequestDto.builder()
                .paymentId(1L)
                .refundAmount(15000) // 결제 금액보다 큰 환불 요청
                .refundReason("상품 불량")
                .build();
        
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.processRefund(invalidRequestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환불 금액이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 처리 - 음수 환불 금액")
    void processRefund_NegativeRefundAmount() {
        // Given
        MoriCashPayment payment = createTestPayment();
        MoriCashRefundRequestDto negativeRequestDto = MoriCashRefundRequestDto.builder()
                .paymentId(1L)
                .refundAmount(-1000) // 음수 환불 요청
                .refundReason("상품 불량")
                .build();
        
        when(moriCashPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.processRefund(negativeRequestDto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환불 금액이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 취소 - 성공")
    void cancelRefund_Success() {
        // Given
        Long paymentId = 1L;
        String cancellationReason = "오류로 인한 취소";
        MoriCashPayment payment = MoriCashPayment.builder()
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .refundPrice(5000) // 환불된 상태
                .build();
        MoriCashBalance balance = createTestBalance(15000);
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        moriCashRefundService.cancelRefund(paymentId, cancellationReason);

        // Then
        verify(moriCashPaymentRepository).findById(paymentId);
        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
        
        assertThat(payment.getRefundPrice()).isNull();
        assertThat(payment.getRefundId()).isNull();
        assertThat(payment.getCancellationReason()).isEqualTo(cancellationReason);
    }

    @Test
    @DisplayName("모리캐시 환불 취소 - 환불되지 않은 결제")
    void cancelRefund_NotRefundedPayment() {
        // Given
        Long paymentId = 1L;
        MoriCashPayment payment = createTestPayment();
        // refundPrice가 null (환불되지 않음)
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.cancelRefund(paymentId, "취소"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환불 처리된 결제가 아닙니다.");
    }

    @Test
    @DisplayName("모리캐시 환불 상세 조회 - 성공")
    void getRefund_Success() {
        // Given
        Long paymentId = 1L;
        Order testOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123")
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(1)
                .totalAmount(new java.math.BigDecimal("10000"))
                .shippingFee(new java.math.BigDecimal("0"))
                .finalAmount(new java.math.BigDecimal("10000"))
                .build();
        try {
            Field orderIdField = testOrder.getClass().getSuperclass().getDeclaredField("id");
            orderIdField.setAccessible(true);
            orderIdField.set(testOrder, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        MoriCashPayment payment = MoriCashPayment.builder()
                .order(testOrder)
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .status(MoriCashPaymentStatus.COMPLETED)
                .description("상품 구매")
                .balanceAfter(0)
                .refundPrice(5000)
                .build();
        try {
            Field idField = payment.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, paymentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MoriCashBalance balance = createTestBalance(10000);
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));

        // When
        MoriCashRefundResponseDto result = moriCashRefundService.getRefund(paymentId, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getRefundPrice()).isEqualTo(5000);
        verify(moriCashPaymentRepository).findById(paymentId);
    }

    @Test
    @DisplayName("모리캐시 환불 상세 조회 - 본인 결제가 아닌 경우")
    void getRefund_NotOwnPayment() {
        // Given
        Long paymentId = 1L;
        User otherUser = createOtherUser();
        MoriCashPayment payment = MoriCashPayment.builder()
                .user(otherUser)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .build();
        
        when(moriCashPaymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> moriCashRefundService.getRefund(paymentId, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 결제만 조회할 수 있습니다.");
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

    private MoriCashRefundRequestDto createTestRequestDto() {
        return MoriCashRefundRequestDto.builder()
                .paymentId(1L)
                .refundAmount(5000)
                .refundReason("상품 불량")
                .build();
    }

    private MoriCashPayment createTestPayment() {
        Order testOrder = Order.builder()
                .user(user)
                .orderNumber("ORD123")
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(1)
                .totalAmount(new java.math.BigDecimal("10000"))
                .shippingFee(new java.math.BigDecimal("0"))
                .finalAmount(new java.math.BigDecimal("10000"))
                .build();
        
        return MoriCashPayment.builder()
                .order(testOrder)
                .user(user)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .transactionType(TransactionType.PURCHASE)
                .status(MoriCashPaymentStatus.COMPLETED)
                .description("상품 구매")
                .balanceAfter(0)
                .build();
    }

    private MoriCashBalance createTestBalance(int totalBalance) {
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(totalBalance)
                .availableBalance(totalBalance)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        return balance;
    }

}
