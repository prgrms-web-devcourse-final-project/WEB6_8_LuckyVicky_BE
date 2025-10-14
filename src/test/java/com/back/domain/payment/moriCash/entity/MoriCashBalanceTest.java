package com.back.domain.payment.moriCash.entity;

import com.back.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoriCashBalanceTest {

    @Test
    @DisplayName("정산 처리 - 모리캐시 차감 및 통계 업데이트")
    void processSettlement() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        MoriCashBalance balance = MoriCashBalance.createInitialBalance(artist);
        balance.addBalance(100000); // 10만원 충전

        // when
        balance.processSettlement(64000); // 64000원 환전

        // then
        assertThat(balance.getTotalBalance()).isEqualTo(36000); // 100000 - 64000
        assertThat(balance.getAvailableBalance()).isEqualTo(36000);
        assertThat(balance.getTotalSettlementSales()).isEqualTo(64000);
        assertThat(balance.getTotalSettlementCommission()).isEqualTo(0); // 환전 시 수수료 없음
        assertThat(balance.getTotalSettlementNetIncome()).isEqualTo(64000);
    }

    @Test
    @DisplayName("정산 처리 실패 - 잔액 부족")
    void processSettlement_InsufficientBalance() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        MoriCashBalance balance = MoriCashBalance.createInitialBalance(artist);
        balance.addBalance(50000); // 5만원만 충전

        // when & then
        assertThatThrownBy(() -> balance.processSettlement(100000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("환전 가능한 모리캐시가 부족합니다.");
    }

    @Test
    @DisplayName("정산 처리 실패 - 음수 금액")
    void processSettlement_NegativeAmount() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        MoriCashBalance balance = MoriCashBalance.createInitialBalance(artist);
        balance.addBalance(100000);

        // when & then
        assertThatThrownBy(() -> balance.processSettlement(-10000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환전 금액은 양수여야 합니다.");
    }

    @Test
    @DisplayName("여러 번 정산 - 통계 누적")
    void processSettlement_Multiple() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        MoriCashBalance balance = MoriCashBalance.createInitialBalance(artist);
        balance.addBalance(200000); // 20만원 충전

        // when
        balance.processSettlement(50000); // 1차 정산 (환전)
        balance.processSettlement(30000); // 2차 정산 (환전)

        // then
        assertThat(balance.getAvailableBalance()).isEqualTo(120000); // 200000 - 50000 - 30000
        assertThat(balance.getTotalSettlementSales()).isEqualTo(80000); // 50000 + 30000
        assertThat(balance.getTotalSettlementCommission()).isEqualTo(0); // 환전 시 수수료 없음
        assertThat(balance.getTotalSettlementNetIncome()).isEqualTo(80000); // 50000 + 30000
    }
}
