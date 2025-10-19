package com.back.domain.payment.settlement.service;

import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.payment.settlement.dto.request.WithdrawalRequestDto;
import com.back.domain.payment.settlement.dto.response.WithdrawalResponseDto;
import com.back.domain.payment.settlement.entity.Settlement;
import com.back.domain.payment.settlement.entity.SettlementStatus;
import com.back.domain.payment.settlement.repository.SettlementRepository;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @InjectMocks
    private SettlementService settlementService;

    private User artist;
    private User normalUser;
    private MoriCashBalance balance;

    @BeforeEach
    void setUp() {
        // 작가 사용자 생성
        artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist(); // 작가 권한 부여

        // 일반 사용자 생성
        normalUser = User.createLocalUser("user@test.com", "password", "일반유저", "010-9876-5432");

        // 모리캐시 잔액 생성
        balance = MoriCashBalance.createInitialBalance(artist);
        balance.addBalance(100000); // 10만원 충전
    }

    @Test
    @DisplayName("환전 요청 성공 - 정상적으로 환전 처리")
    void requestWithdrawal_Success() {
        // given
        WithdrawalRequestDto requestDto = WithdrawalRequestDto.builder()
                .amount(64000)
                .build();

        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(64000)
                .commissionRate(0)  // 환전 시 수수료 없음
                .bankName("데모은행")
                .accountNumber("000-0000-0000")
                .accountHolder("작가")
                .build();

        when(moriCashBalanceRepository.findByUserWithLock(artist)).thenReturn(Optional.of(balance));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

        // when
        WithdrawalResponseDto response = settlementService.requestWithdrawal(requestDto, artist);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRequestedAmount()).isEqualTo(64000);
        assertThat(response.getCommissionAmount()).isEqualTo(0); // 환전 시 수수료 없음
        assertThat(response.getNetAmount()).isEqualTo(64000); // 신청 금액 그대로 지급
        assertThat(response.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(response.getRemainingBalance()).isEqualTo(36000); // 100000 - 64000

        // 모리캐시 차감 확인
        assertThat(balance.getAvailableBalance()).isEqualTo(36000);
        assertThat(balance.getTotalSettlementSales()).isEqualTo(64000);
        assertThat(balance.getTotalSettlementCommission()).isEqualTo(0); // 환전 시 수수료 없음
        assertThat(balance.getTotalSettlementNetIncome()).isEqualTo(64000); // 신청 금액 그대로

        verify(settlementRepository, times(1)).save(any(Settlement.class));
        verify(moriCashBalanceRepository, times(1)).save(balance);
    }

    @Test
    @DisplayName("환전 요청 실패 - 작가 권한 없음")
    void requestWithdrawal_Fail_NotArtist() {
        // given
        WithdrawalRequestDto requestDto = WithdrawalRequestDto.builder()
                .amount(64000)
                .build();

        // when & then
        assertThatThrownBy(() -> settlementService.requestWithdrawal(requestDto, normalUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작가만 환전 신청이 가능합니다.");

        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    @DisplayName("환전 요청 실패 - 모리캐시 부족")
    void requestWithdrawal_Fail_InsufficientBalance() {
        // given
        WithdrawalRequestDto requestDto = WithdrawalRequestDto.builder()
                .amount(200000) // 보유액보다 많은 금액
                .build();

        when(moriCashBalanceRepository.findByUserWithLock(artist)).thenReturn(Optional.of(balance));

        // when & then
        assertThatThrownBy(() -> settlementService.requestWithdrawal(requestDto, artist))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환전 가능한 모리캐시가 부족합니다.");

        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    @DisplayName("환전 요청 실패 - 모리캐시 잔액이 없을 때 자동 생성되지만 잔액 부족")
    void requestWithdrawal_Fail_CreateBalanceButInsufficient() {
        // given
        WithdrawalRequestDto requestDto = WithdrawalRequestDto.builder()
                .amount(10000)
                .build();

        // 잔액이 없을 때 빈 잔액 생성 (0원)
        MoriCashBalance newBalance = MoriCashBalance.createInitialBalance(artist);

        when(moriCashBalanceRepository.findByUserWithLock(artist)).thenReturn(Optional.empty());
        when(moriCashBalanceRepository.save(any(MoriCashBalance.class))).thenReturn(newBalance);

        // when & then
        assertThatThrownBy(() -> settlementService.requestWithdrawal(requestDto, artist))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("환전 가능한 모리캐시가 부족합니다.");

        // 잔액은 생성되었지만 환전은 실패
        verify(moriCashBalanceRepository, times(1)).save(any(MoriCashBalance.class));
        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    @DisplayName("수수료 계산 확인 - 환전 시 수수료 없음")
    void commissionCalculation() {
        // given
        WithdrawalRequestDto requestDto = WithdrawalRequestDto.builder()
                .amount(50000)
                .build();

        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(50000)
                .commissionRate(0)  // 환전 시 수수료 없음
                .bankName("데모은행")
                .accountNumber("000-0000-0000")
                .accountHolder("작가")
                .build();

        when(moriCashBalanceRepository.findByUserWithLock(artist)).thenReturn(Optional.of(balance));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

        // when
        WithdrawalResponseDto response = settlementService.requestWithdrawal(requestDto, artist);

        // then
        assertThat(response.getCommissionAmount()).isEqualTo(0); // 환전 시 수수료 없음
        assertThat(response.getNetAmount()).isEqualTo(50000); // 신청 금액 그대로
    }

    @Test
    @DisplayName("보유 모리캐시 조회")
    void getMoriCashBalance() {
        // given
        when(moriCashBalanceRepository.findByUser(artist)).thenReturn(Optional.of(balance)); // 조회 전용이라 락 없음

        // when
        Integer result = settlementService.getMoriCashBalance(artist);

        // then
        assertThat(result).isEqualTo(100000);
    }

    @Test
    @DisplayName("보유 모리캐시 조회 - 잔액 없을 때 0 반환")
    void getMoriCashBalance_NoBalance() {
        // given
        when(moriCashBalanceRepository.findByUser(artist)).thenReturn(Optional.empty()); // 조회 전용이라 락 없음

        // when
        Integer result = settlementService.getMoriCashBalance(artist);

        // then
        assertThat(result).isEqualTo(0);
    }
}
