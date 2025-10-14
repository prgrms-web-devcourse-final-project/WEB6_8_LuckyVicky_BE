package com.back.domain.payment.settlement.entity;

import com.back.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SettlementTest {

    @Test
    @DisplayName("Settlement 생성 - 즉시 완료 상태 (환전 시 수수료 없음)")
    void createSettlement() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        // when - 환전 요청 (수수료 없음)
        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(64000)
                .commissionRate(0)  // 환전 시 수수료 없음 (빌더에서 0으로 고정)
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .build();

        // then
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(settlement.getCompletedAt()).isNotNull();
        assertThat(settlement.getRequestedAmount()).isEqualTo(64000);
        assertThat(settlement.getCommissionAmount()).isEqualTo(0);  // 환전 시 수수료 없음
        assertThat(settlement.getNetAmount()).isEqualTo(64000);  // 신청 금액 그대로
    }

    @Test
    @DisplayName("수수료 계산 - 환전 시 수수료 없음")
    void calculateCommission() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();

        // when - 환전 요청 (수수료 없음)
        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(100000)
                .commissionRate(0)  // 환전 시 수수료 없음 (빌더에서 0으로 고정)
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .build();

        // then - 환전 시 수수료 없이 신청 금액 그대로 지급
        assertThat(settlement.getCommissionAmount()).isEqualTo(0);
        assertThat(settlement.getNetAmount()).isEqualTo(100000);
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
                .commissionRate(0)  // 환전 시 수수료 없음
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .build();

        // when & then - 환전 요청은 즉시 완료 상태
        assertThat(settlement.isCompleted()).isTrue();
    }
}
