package com.back.domain.payment.cash.service;

import com.back.domain.payment.cash.dto.request.CashExchangeRequestDto;
import com.back.domain.payment.cash.dto.response.CashExchangeResponseDto;
import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import com.back.domain.payment.cash.entity.CashTransactionType;
import com.back.domain.payment.cash.repository.CashTransactionRepository;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.user.entity.Role;
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
class CashExchangeServiceTest {

    @Mock
    private CashTransactionRepository cashTransactionRepository;

    @Mock
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @InjectMocks
    private CashExchangeService cashExchangeService;

    private User artistUser;
    private User regularUser;
    private CashExchangeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        artistUser = createTestArtistUser();
        regularUser = createTestRegularUser();
        requestDto = createTestRequestDto();
    }

    @Test
    @DisplayName("캐시 환전 신청 - 작가 성공")
    void createExchangeRequest_ArtistSuccess() {
        // Given
        MoriCashBalance balance = createTestBalance(15000);
        CashTransaction savedTransaction = createTestTransaction();
        
        when(moriCashBalanceRepository.findByUserWithLock(artistUser)).thenReturn(Optional.of(balance));
        when(cashTransactionRepository.save(any(CashTransaction.class))).thenReturn(savedTransaction);
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        CashExchangeResponseDto result = cashExchangeService.createExchangeRequest(requestDto, artistUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(10000);
        assertThat(result.getStatus()).isEqualTo(CashTransactionStatus.PENDING);

        verify(moriCashBalanceRepository).findByUserWithLock(artistUser);
        verify(cashTransactionRepository).save(any(CashTransaction.class));
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
    }

    @Test
    @DisplayName("캐시 환전 신청 - 일반 사용자 권한 없음")
    void createExchangeRequest_RegularUserUnauthorized() {
        // When & Then
        assertThatThrownBy(() -> cashExchangeService.createExchangeRequest(requestDto, regularUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작가만 환전이 가능합니다.");
    }

    @Test
    @DisplayName("캐시 환전 신청 - 잔액 부족")
    void createExchangeRequest_InsufficientBalance() {
        // Given
        MoriCashBalance balance = createTestBalance(5000); // 요청 금액보다 적음
        
        when(moriCashBalanceRepository.findByUserWithLock(artistUser)).thenReturn(Optional.of(balance));

        // When & Then
        assertThatThrownBy(() -> cashExchangeService.createExchangeRequest(requestDto, artistUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환전 가능한 모리캐시가 부족합니다.");
    }

    @Test
    @DisplayName("캐시 환전 신청 - 잔액 정보 없음")
    void createExchangeRequest_BalanceNotFound() {
        // Given
        when(moriCashBalanceRepository.findByUserWithLock(artistUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cashExchangeService.createExchangeRequest(requestDto, artistUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모리캐시 잔액 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("환전 승인 처리 - 성공")
    void approveExchange_Success() {
        // Given
        Long transactionId = 1L;
        String pgTransactionId = "PG_12345";
        String pgApprovalNumber = "APP_67890";
        
        CashTransaction transaction = createTestTransaction();
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(artistUser)
                .totalBalance(15000)
                .availableBalance(5000)
                .frozenBalance(10000) // 환전 신청 시 동결된 금액
                .totalCharged(0)
                .totalUsed(0)
                .build();
        
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(moriCashBalanceRepository.findByUserWithLock(artistUser)).thenReturn(Optional.of(balance));
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        CashExchangeResponseDto result = cashExchangeService.approveExchange(transactionId, pgTransactionId, pgApprovalNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CashTransactionStatus.COMPLETED);

        verify(cashTransactionRepository).findById(transactionId);
        verify(moriCashBalanceRepository).findByUserWithLock(artistUser);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
    }

    @Test
    @DisplayName("환전 승인 처리 - 거래를 찾을 수 없는 경우")
    void approveExchange_TransactionNotFound() {
        // Given
        Long transactionId = 999L;
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cashExchangeService.approveExchange(transactionId, "PG_123", "APP_456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("캐시 거래를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("환전 거부 처리 - 성공")
    void rejectExchange_Success() {
        // Given
        Long transactionId = 1L;
        String rejectionReason = "서류 미비";
        CashTransaction transaction = createTestTransaction();
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(artistUser)
                .totalBalance(15000)
                .availableBalance(5000)
                .frozenBalance(10000) // 환전 신청 시 동결된 금액
                .totalCharged(0)
                .totalUsed(0)
                .build();
        
        when(cashTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(moriCashBalanceRepository.findByUserWithLock(artistUser)).thenReturn(Optional.of(balance));
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(balance);

        // When
        cashExchangeService.rejectExchange(transactionId, rejectionReason);

        // Then
        verify(cashTransactionRepository).findById(transactionId);
        verify(moriCashBalanceRepository).findByUserWithLock(artistUser);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
        
        assertThat(transaction.getStatus()).isEqualTo(CashTransactionStatus.FAILED);
        assertThat(transaction.getFailureReason()).isEqualTo(rejectionReason);
    }

    // Helper Methods
    private User createTestArtistUser() {
        // User 엔티티에 setRole이 없으므로 reflection 사용
        User user = User.createLocalUser(
                "artist@example.com",
                "password123",
                "작가유저",
                "010-1234-5678"
        );
        try {
            Field roleField = User.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(user, com.back.domain.user.entity.Role.ARTIST);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set role", e);
        }
        return user;
    }

    private User createTestRegularUser() {
        return User.createLocalUser(
                "user@example.com",
                "password123",
                "일반유저",
                "010-1234-5678"
        );
    }

    private CashExchangeRequestDto createTestRequestDto() {
        return CashExchangeRequestDto.builder()
                .amount(10000)
                .bankName("국민은행")
                .accountNumber("123456-78-901234")
                .accountHolder("홍길동")
                .build();
    }

    private CashTransaction createTestTransaction() {
        CashTransaction transaction = CashTransaction.builder()
                .user(artistUser)
                .amount(10000)
                .transactionType(CashTransactionType.EXCHANGE)
                .paymentMethod("계좌이체")
                .pgProvider("INTERNAL")
                .balanceAfter(5000)
                .build();
        return transaction;
    }

    private MoriCashBalance createTestBalance(int availableBalance) {
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(artistUser)
                .totalBalance(availableBalance)
                .availableBalance(availableBalance)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        return balance;
    }

}
