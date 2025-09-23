package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistCashResponse DTO 테스트
 * Builder 패턴과 데이터 구조의 정확성을 검증
 * 2025.09.23 생성
 */
@DisplayName("ArtistCashResponse DTO 테스트")
public class ArtistCashResponseTest {

    @Test
    @DisplayName("Balance Builder 패턴 테스트")
    void builder_Balance_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ArtistCashResponse.Balance balance = ArtistCashResponse.Balance.builder()
                .currentBalance(500000)
                .pendingSettlement(150000)
                .pendingWithdrawal(50000)
                .withdrawable(300000)
                .currency("KRW")
                .updatedAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(balance).isNotNull(),
                () -> assertThat(balance.getCurrentBalance()).isEqualTo(500000),
                () -> assertThat(balance.getPendingSettlement()).isEqualTo(150000),
                () -> assertThat(balance.getPendingWithdrawal()).isEqualTo(50000),
                () -> assertThat(balance.getWithdrawable()).isEqualTo(300000),
                () -> assertThat(balance.getCurrency()).isEqualTo("KRW"),
                () -> assertThat(balance.getUpdatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("지갑 잔액 0원 테스트")
    void builder_Balance_ZeroAmount() {
        // When
        ArtistCashResponse.Balance balance = ArtistCashResponse.Balance.builder()
                .currentBalance(0)
                .pendingSettlement(0)
                .pendingWithdrawal(0)
                .withdrawable(0)
                .currency("KRW")
                .updatedAt(LocalDateTime.now())
                .build();

        // Then
        assertAll(
                () -> assertThat(balance.getCurrentBalance()).isEqualTo(0),
                () -> assertThat(balance.getPendingSettlement()).isEqualTo(0),
                () -> assertThat(balance.getPendingWithdrawal()).isEqualTo(0),
                () -> assertThat(balance.getWithdrawable()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("지갑 잔액 최대값 테스트")
    void builder_Balance_MaxAmount() {
        // Given
        int maxAmount = Integer.MAX_VALUE;
        
        // When
        ArtistCashResponse.Balance balance = ArtistCashResponse.Balance.builder()
                .currentBalance(maxAmount)
                .pendingSettlement(maxAmount)
                .pendingWithdrawal(maxAmount)
                .withdrawable(maxAmount)
                .currency("KRW")
                .updatedAt(LocalDateTime.now())
                .build();

        // Then
        assertAll(
                () -> assertThat(balance.getCurrentBalance()).isEqualTo(maxAmount),
                () -> assertThat(balance.getPendingSettlement()).isEqualTo(maxAmount),
                () -> assertThat(balance.getPendingWithdrawal()).isEqualTo(maxAmount),
                () -> assertThat(balance.getWithdrawable()).isEqualTo(maxAmount)
        );
    }

    @Test
    @DisplayName("다른 통화 단위 테스트")
    void builder_Balance_DifferentCurrency() {
        // When
        ArtistCashResponse.Balance balance = ArtistCashResponse.Balance.builder()
                .currentBalance(1000)
                .pendingSettlement(200)
                .pendingWithdrawal(100)
                .withdrawable(700)
                .currency("USD")
                .updatedAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(balance.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("API 명세와 동일한 구조 생성 테스트")
    void createApiResponseStructure() {
        // When
        ArtistCashResponse.Balance response = ArtistCashResponse.Balance.builder()
                .currentBalance(750000)
                .pendingSettlement(250000)
                .pendingWithdrawal(100000)
                .withdrawable(400000)
                .currency("KRW")
                .updatedAt(LocalDateTime.of(2025, 9, 22, 14, 30, 0))
                .build();

        // Then
        assertAll(
                () -> assertThat(response.getCurrentBalance()).isEqualTo(750000),
                () -> assertThat(response.getPendingSettlement()).isEqualTo(250000),
                () -> assertThat(response.getPendingWithdrawal()).isEqualTo(100000),
                () -> assertThat(response.getWithdrawable()).isEqualTo(400000),
                () -> assertThat(response.getCurrency()).isEqualTo("KRW"),
                () -> assertThat(response.getUpdatedAt()).isNotNull()
        );
    }
}
