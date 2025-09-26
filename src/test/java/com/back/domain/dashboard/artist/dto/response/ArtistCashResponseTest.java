package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistCashResponse DTO 테스트
 * 핵심 비즈니스 로직에 집중
 * 2025.09.25 수정
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
                () -> assertThat(balance.currentBalance()).isNotNegative(),
                () -> assertThat(balance.pendingSettlement()).isNotNegative(),
                () -> assertThat(balance.pendingWithdrawal()).isNotNegative(),
                () -> assertThat(balance.withdrawable()).isNotNegative(),
                () -> assertThat(balance.currency()).isNotBlank(),
                () -> assertThat(balance.updatedAt()).isEqualTo(now)
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
                () -> assertThat(balance.withdrawable()).isLessThanOrEqualTo(balance.currentBalance()),
                // 통화는 KRW 또는 USD
                () -> assertThat(balance.currency()).isIn("KRW", "USD"),
                // 업데이트 시간은 현재보다 과거여야 함
                () -> assertThat(balance.updatedAt()).isBeforeOrEqualTo(LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistCashResponse.Balance response = new ArtistCashResponse.Balance(
                750000,
                250000,
                100000,
                400000,
                "KRW",
                LocalDateTime.of(2025, 9, 22, 14, 30, 0)
        );

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(response.currentBalance()).isEqualTo(750000),
                () -> assertThat(response.pendingSettlement()).isEqualTo(250000),
                () -> assertThat(response.pendingWithdrawal()).isEqualTo(100000),
                () -> assertThat(response.withdrawable()).isEqualTo(400000),
                () -> assertThat(response.currency()).isEqualTo("KRW"),
                () -> assertThat(response.updatedAt()).isNotNull()
        );
    }

    // ------------ 헬퍼 메서드들 --------------

    private ArtistCashResponse.Balance createSampleBalance(LocalDateTime updatedAt) {
        return new ArtistCashResponse.Balance(
                500000,
                150000,
                50000,
                300000,
                "KRW",
                updatedAt
        );
    }
}
