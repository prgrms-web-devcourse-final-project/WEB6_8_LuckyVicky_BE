package com.back.domain.payment.moriCash.service;

import com.back.domain.payment.moriCash.dto.response.MoriCashBalanceResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoriCashBalanceServiceTest {

    @Mock
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @Mock
    private MoriCashPaymentRepository moriCashPaymentRepository;

    @InjectMocks
    private MoriCashBalanceService moriCashBalanceService;

    private User user;

    @BeforeEach
    void setUp() {
        user = createTestUser();
    }

    @Test
    @DisplayName("모리캐시 잔액 조회 - 기존 잔액이 있는 경우")
    void getBalance_ExistingBalance() {
        // Given
        MoriCashBalance balance = createTestBalance();
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashPaymentRepository.getTotalChargedAmountByUser(user)).thenReturn(50000);
        when(moriCashPaymentRepository.getTotalUsedAmountByUser(user)).thenReturn(20000);

        // When
        MoriCashBalanceResponseDto result = moriCashBalanceService.getBalance(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTotalBalance()).isEqualTo(30000);
        assertThat(result.getAvailableBalance()).isEqualTo(25000);
        assertThat(result.getFrozenBalance()).isEqualTo(5000);
        assertThat(result.getTotalCharged()).isEqualTo(50000);
        assertThat(result.getTotalUsed()).isEqualTo(20000);

        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashPaymentRepository).getTotalChargedAmountByUser(user);
        verify(moriCashPaymentRepository).getTotalUsedAmountByUser(user);
    }

    @Test
    @DisplayName("모리캐시 잔액 조회 - 기존 잔액이 없는 경우 새로 생성")
    void getBalance_CreateNewBalance() {
        // Given
        MoriCashBalance newBalance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(0)
                .availableBalance(0)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.empty());
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(newBalance);
        when(moriCashPaymentRepository.getTotalChargedAmountByUser(user)).thenReturn(0);
        when(moriCashPaymentRepository.getTotalUsedAmountByUser(user)).thenReturn(0);

        // When
        MoriCashBalanceResponseDto result = moriCashBalanceService.getBalance(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalBalance()).isEqualTo(0);
        assertThat(result.getAvailableBalance()).isEqualTo(0);
        assertThat(result.getFrozenBalance()).isEqualTo(0);
        assertThat(result.getTotalCharged()).isEqualTo(0);
        assertThat(result.getTotalUsed()).isEqualTo(0);

        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashBalanceRepository).save(any(MoriCashBalance.class));
        verify(moriCashPaymentRepository).getTotalChargedAmountByUser(user);
        verify(moriCashPaymentRepository).getTotalUsedAmountByUser(user);
    }

    @Test
    @DisplayName("모리캐시 잔액 조회 - 총 충전/사용 금액이 null인 경우")
    void getBalance_NullChargedAndUsedAmounts() {
        // Given
        MoriCashBalance balance = createTestBalance();
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashPaymentRepository.getTotalChargedAmountByUser(user)).thenReturn(null);
        when(moriCashPaymentRepository.getTotalUsedAmountByUser(user)).thenReturn(null);

        // When
        MoriCashBalanceResponseDto result = moriCashBalanceService.getBalance(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCharged()).isEqualTo(0);
        assertThat(result.getTotalUsed()).isEqualTo(0);

        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashPaymentRepository).getTotalChargedAmountByUser(user);
        verify(moriCashPaymentRepository).getTotalUsedAmountByUser(user);
    }

    @Test
    @DisplayName("모리캐시 잔액 이력 조회 - 성공")
    void getBalanceWithHistory_Success() {
        // Given
        MoriCashBalance balance = createTestBalance();
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashPaymentRepository.getTotalChargedAmountByUser(user)).thenReturn(50000);
        when(moriCashPaymentRepository.getTotalUsedAmountByUser(user)).thenReturn(20000);

        // When
        MoriCashBalanceResponseDto result = moriCashBalanceService.getBalanceWithHistory(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTotalBalance()).isEqualTo(30000);

        verify(moriCashBalanceRepository).findByUser(user);
        verify(moriCashPaymentRepository).getTotalChargedAmountByUser(user);
        verify(moriCashPaymentRepository).getTotalUsedAmountByUser(user);
    }

    @Test
    @DisplayName("모리캐시 잔액 조회 - 대량 충전/사용 금액")
    void getBalance_LargeAmounts() {
        // Given
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(1000000)
                .availableBalance(800000)
                .frozenBalance(200000)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashPaymentRepository.getTotalChargedAmountByUser(user)).thenReturn(2000000);
        when(moriCashPaymentRepository.getTotalUsedAmountByUser(user)).thenReturn(1000000);

        // When
        MoriCashBalanceResponseDto result = moriCashBalanceService.getBalance(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalBalance()).isEqualTo(1000000);
        assertThat(result.getAvailableBalance()).isEqualTo(800000);
        assertThat(result.getFrozenBalance()).isEqualTo(200000);
        assertThat(result.getTotalCharged()).isEqualTo(2000000);
        assertThat(result.getTotalUsed()).isEqualTo(1000000);
    }

    @Test
    @DisplayName("모리캐시 잔액 조회 - 제로 잔액")
    void getBalance_ZeroBalance() {
        // Given
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(0)
                .availableBalance(0)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        
        when(moriCashBalanceRepository.findByUser(user)).thenReturn(Optional.of(balance));
        when(moriCashPaymentRepository.getTotalChargedAmountByUser(user)).thenReturn(0);
        when(moriCashPaymentRepository.getTotalUsedAmountByUser(user)).thenReturn(0);

        // When
        MoriCashBalanceResponseDto result = moriCashBalanceService.getBalance(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalBalance()).isEqualTo(0);
        assertThat(result.getAvailableBalance()).isEqualTo(0);
        assertThat(result.getFrozenBalance()).isEqualTo(0);
        assertThat(result.getTotalCharged()).isEqualTo(0);
        assertThat(result.getTotalUsed()).isEqualTo(0);
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
            java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private MoriCashBalance createTestBalance() {
        MoriCashBalance balance = MoriCashBalance.builder()
                .user(user)
                .totalBalance(30000)
                .availableBalance(25000)
                .frozenBalance(5000)
                .totalCharged(0)
                .totalUsed(0)
                .build();
        return balance;
    }

}
