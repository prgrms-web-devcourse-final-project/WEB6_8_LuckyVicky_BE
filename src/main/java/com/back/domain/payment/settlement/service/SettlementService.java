package com.back.domain.payment.settlement.service;

import com.back.domain.payment.settlement.dto.request.WithdrawalRequestDto;
import com.back.domain.payment.settlement.dto.response.WithdrawalResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.payment.settlement.entity.Settlement;
import com.back.domain.payment.settlement.repository.SettlementRepository;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final MoriCashBalanceRepository moriCashBalanceRepository;

    /**
     * 환전 요청 (즉시 완료 처리)
     */
    public WithdrawalResponseDto requestWithdrawal(WithdrawalRequestDto requestDto, User artist) {
        log.info("환전 요청 시작 - 작가ID: {}, 금액: {}", artist.getId(), requestDto.getAmount());

        // 0. 작가 권한 확인
        if (!artist.isArtist()) {
            log.error("작가 권한 없음 - 사용자ID: {}, 역할: {}", artist.getId(), artist.getRole());
            throw new IllegalArgumentException("작가만 환전 신청이 가능합니다.");
        }

        // 1. 모리캐시 잔액 조회 또는 생성 (Pessimistic Write Lock - 동시성 제어)
        MoriCashBalance balance = moriCashBalanceRepository.findByUserWithLock(artist)
                .orElseGet(() -> {
                    log.info("모리캐시 잔액 정보 없음. 새로 생성 - 작가ID: {}", artist.getId());
                    MoriCashBalance newBalance = MoriCashBalance.createInitialBalance(artist);
                    return moriCashBalanceRepository.save(newBalance);
                });

        // 2. 모리캐시 잔액 검증
        if (!balance.hasAvailableBalance(requestDto.getAmount())) {
            log.error("모리캐시 부족 - 작가ID: {}, 보유: {}, 요청: {}", 
                    artist.getId(), balance.getAvailableBalance(), requestDto.getAmount());
            throw new IllegalArgumentException("환전 가능한 모리캐시가 부족합니다.");
        }

        // 3. 정산 레코드 생성 (즉시 완료 상태, 계좌 정보는 더미 데이터)
        Settlement settlement = Settlement.builder()
                .artist(artist)
                .requestedAmount(requestDto.getAmount())
                .commissionRate(10) // 수수료율 10%
                .bankName("데모은행") // 영상용 더미 데이터
                .accountNumber("000-0000-0000") // 영상용 더미 데이터
                .accountHolder(artist.getName()) // 사용자 이름 사용
                .build();

        settlement = settlementRepository.save(settlement);
        log.info("정산 레코드 생성 완료 - 정산ID: {}", settlement.getId());

        // 4. 모리캐시 잔액 업데이트 (차감 및 통계 업데이트)
        balance.processSettlement(settlement.getRequestedAmount());
        moriCashBalanceRepository.save(balance);

        log.info("환전 완료 - 정산ID: {}, 환전금액: {}, 수수료: {}, 순수익: {}, 잔여 모리캐시: {}", 
                settlement.getId(), 
                settlement.getRequestedAmount(), 
                settlement.getCommissionAmount(),
                settlement.getNetAmount(),
                balance.getAvailableBalance());

        return WithdrawalResponseDto.from(settlement, balance.getAvailableBalance());
    }

    /**
     * 보유 모리캐시 조회
     */
    @Transactional(readOnly = true)
    public Integer getMoriCashBalance(User artist) {
        return moriCashBalanceRepository.findByUser(artist)
                .map(MoriCashBalance::getAvailableBalance)
                .orElse(0);
    }
}
