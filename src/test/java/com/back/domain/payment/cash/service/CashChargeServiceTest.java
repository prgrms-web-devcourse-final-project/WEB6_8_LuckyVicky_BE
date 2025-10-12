package com.back.domain.payment.cash.service;

import com.back.domain.payment.cash.dto.request.CashChargeRequestDto;
import com.back.domain.payment.cash.dto.response.CashChargeResponseDto;
import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import com.back.domain.payment.cash.entity.CashTransactionType;
import com.back.domain.payment.cash.repository.CashTransactionRepository;
import com.back.domain.payment.gateway.dto.TossPaymentApproveResponse;
import com.back.domain.payment.gateway.service.PaymentGatewayService;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashChargeServiceTest {

    @Mock
    private CashTransactionRepository cashTransactionRepository;

    @Mock
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private CashChargeService cashChargeService;

    private User user;
    private CashChargeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = createTestUser();
        requestDto = createTestRequestDto();
    }

    @Test
    @DisplayName("캐시 충전 신청 - 성공")
    void createChargeRequest_Success() {
        // Given
        CashTransaction savedTransaction = createTestTransaction();
        when(cashTransactionRepository.save(any(CashTransaction.class))).thenReturn(savedTransaction);

        // When
        CashChargeResponseDto result = cashChargeService.createChargeRequest(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(10000);
        assertThat(result.getStatus()).isEqualTo(CashTransactionStatus.PENDING);

        verify(cashTransactionRepository).save(any(CashTransaction.class));
    }

    @Test
    @DisplayName("캐시 충전 완료 처리 - 성공")
    void completeCharge_Success() {
        // Given
        Long transactionId = 1L;
        String paymentKey = "pay_test123";
        String orderId = "order_test123";
        
        CashTransaction transaction = createTestTransaction();
        MoriCashBalance balance = createTestBalance();
        
        TossPaymentApproveResponse pgResponse = TossPaymentApproveResponse.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .status("DONE")
                .totalAmount(10000)
                .approvedAt(LocalDateTime.now())
                .build();
        
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(paymentGatewayService.approvePayment(anyString(), anyString(), anyInt())).thenReturn(pgResponse);
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        CashChargeResponseDto result = cashChargeService.completeCharge(transactionId, paymentKey, orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPgTransactionId()).isEqualTo(paymentKey);
        assertThat(result.getStatus()).isEqualTo(CashTransactionStatus.COMPLETED);

        verify(cashTransactionRepository).findById(transactionId);
        verify(paymentGatewayService).approvePayment(paymentKey, orderId, transaction.getAmount());
        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
    }

    @Test
    @DisplayName("캐시 충전 완료 처리 - 거래를 찾을 수 없는 경우")
    void completeCharge_TransactionNotFound() {
        // Given
        Long transactionId = 999L;
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cashChargeService.completeCharge(transactionId, "PG_123", "APP_456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("캐시 거래를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("캐시 충전 완료 처리 - 잔액이 없는 경우 새로 생성")
    void completeCharge_CreateNewBalance() {
        // Given
        Long transactionId = 1L;
        String paymentKey = "pay_test456";
        String orderId = "order_test456";
        
        CashTransaction transaction = createTestTransaction();
        MoriCashBalance newBalance = createTestBalance();
        
        TossPaymentApproveResponse pgResponse = TossPaymentApproveResponse.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .status("DONE")
                .totalAmount(10000)
                .approvedAt(LocalDateTime.now())
                .build();
        
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(paymentGatewayService.approvePayment(anyString(), anyString(), anyInt())).thenReturn(pgResponse);
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.empty());
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(newBalance);

        // When
        CashChargeResponseDto result = cashChargeService.completeCharge(transactionId, paymentKey, orderId);

        // Then
        assertThat(result).isNotNull();
        verify(paymentGatewayService).approvePayment(paymentKey, orderId, transaction.getAmount());
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
    }

    @Test
    @DisplayName("캐시 충전 실패 처리 - 성공")
    void failCharge_Success() {
        // Given
        Long transactionId = 1L;
        String failureReason = "카드 한도 초과";
        CashTransaction transaction = createTestTransaction();
        
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // When
        cashChargeService.failCharge(transactionId, failureReason);

        // Then
        verify(cashTransactionRepository).findById(transactionId);
        assertThat(transaction.getStatus()).isEqualTo(CashTransactionStatus.FAILED);
        assertThat(transaction.getFailureReason()).isEqualTo(failureReason);
    }

    @Test
    @DisplayName("캐시 충전 취소 - 성공")
    void cancelCharge_Success() {
        // Given
        Long transactionId = 1L;
        String paymentKey = "pay_test123";
        String cancellationReason = "사용자 요청";
        CashTransaction transaction = createTestTransaction();
        
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // When
        cashChargeService.cancelCharge(transactionId, paymentKey, cancellationReason);

        // Then
        verify(cashTransactionRepository).findById(transactionId);
        assertThat(transaction.getStatus()).isEqualTo(CashTransactionStatus.CANCELLED);
        assertThat(transaction.getCancellationReason()).isEqualTo(cancellationReason);
    }

    // Helper Methods
    private User createTestUser() {
        return User.createLocalUser(
                "test@example.com",
                "password123",
                "테스트유저",
                "010-1234-5678"
        );
    }

    private CashChargeRequestDto createTestRequestDto() {
        return CashChargeRequestDto.builder()
                .amount(10000)
                .paymentMethod("토스페이")
                .pgProvider("TOSS")
                .build();
    }

    private CashTransaction createTestTransaction() {
        CashTransaction transaction = CashTransaction.builder()
                .user(user)
                .amount(10000)
                .transactionType(CashTransactionType.CHARGING)
                .paymentMethod("토스페이")
                .pgProvider("TOSS")
                .build();
        return transaction;
    }

    private MoriCashBalance createTestBalance() {
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(5000)
                .availableBalance(5000)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        return balance;
    }

}
