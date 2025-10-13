package com.back.domain.payment.settlement.entity;

import com.back.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SettlementTest {

    @Test
    @DisplayName("Settlement 생성 - 즉시 완료 상태")
    void createSettlement() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        // when
        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(64000)
                .commissionRate(10)
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .build();

        // then
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(settlement.getCompletedAt()).isNotNull();
        assertThat(settlement.getRequestedAmount()).isEqualTo(64000);
        assertThat(settlement.getCommissionAmount()).isEqualTo(6400);
        assertThat(settlement.getNetAmount()).isEqualTo(57600);
    }

    @Test
    @DisplayName("수수료 계산 - 10%")
    void calculateCommission() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        // when
        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(100000)
                .commissionRate(10)
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .build();

        // then
        assertThat(settlement.getCommissionAmount()).isEqualTo(10000);
        assertThat(settlement.getNetAmount()).isEqualTo(90000);
    }

    @Test
    @DisplayName("완료 여부 확인")
    void isCompleted() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(64000)
                .commissionRate(10)
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .build();

        // when & then
        assertThat(settlement.isCompleted()).isTrue();
    }
}
