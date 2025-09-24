package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistCashResponse DTO 테스트
 * 핵심 비즈니스 로직에 집중
 * 2025.09.23 생성
 */
@DisplayName("ArtistCashResponse DTO 테스트")
public class ArtistCashResponseTest {

    @Test
    @DisplayName("지갑 잔액 구조 생성 및 검증")
    void createBalance_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ArtistCashResponse.Balance balance = createSampleBalance(now);

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(balance).isNotNull(),
                () -> assertThat(balance.getCurrentBalance()).isNotNegative(),
                () -> assertThat(balance.getPendingSettlement()).isNotNegative(),
                () -> assertThat(balance.getPendingWithdrawal()).isNotNegative(),
                () -> assertThat(balance.getWithdrawable()).isNotNegative(),
                () -> assertThat(balance.getCurrency()).isNotBlank(),
                () -> assertThat(balance.getUpdatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("지갑 잔액 비즈니스 규칙 검증")
    void validateBusinessRules_Success() {
        // When
        ArtistCashResponse.Balance balance = createSampleBalance(LocalDateTime.now());

        // Then - 비즈니스 규칙 검증
        assertAll(
                // 환전 가능 금액은 현재 잔액보다 클 수 없음
                () -> assertThat(balance.getWithdrawable()).isLessThanOrEqualTo(balance.getCurrentBalance()),
                // 통화는 KRW 또는 USD
                () -> assertThat(balance.getCurrency()).isIn("KRW", "USD"),
                // 업데이트 시간은 현재보다 과거여야 함
                () -> assertThat(balance.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistCashResponse.Balance response = ArtistCashResponse.Balance.builder()
                .currentBalance(750000)
                .pendingSettlement(250000)
                .pendingWithdrawal(100000)
                .withdrawable(400000)
                .currency("KRW")
                .updatedAt(LocalDateTime.of(2025, 9, 22, 14, 30, 0))
                .build();

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(response.getCurrentBalance()).isEqualTo(750000),
                () -> assertThat(response.getPendingSettlement()).isEqualTo(250000),
                () -> assertThat(response.getPendingWithdrawal()).isEqualTo(100000),
                () -> assertThat(response.getWithdrawable()).isEqualTo(400000),
                () -> assertThat(response.getCurrency()).isEqualTo("KRW"),
                () -> assertThat(response.getUpdatedAt()).isNotNull()
        );
    }

    // ------------ 헬퍼 메서드들 --------------

    private ArtistCashResponse.Balance createSampleBalance(LocalDateTime updatedAt) {
        return ArtistCashResponse.Balance.builder()
                .currentBalance(500000)
                .pendingSettlement(150000)
                .pendingWithdrawal(50000)
                .withdrawable(300000)
                .currency("KRW")
                .updatedAt(updatedAt)
                .build();
    }
}
