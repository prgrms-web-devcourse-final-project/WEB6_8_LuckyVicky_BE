package com.back.domain.payment.moriCash.service;

import com.back.domain.payment.moriCash.dto.response.MoriCashBalanceResponseDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.entity.MoriCashPayment;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.payment.moriCash.repository.MoriCashPaymentRepository;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MoriCashBalanceService {

    private final MoriCashBalanceRepository moriCashBalanceRepository;
    private final MoriCashPaymentRepository moriCashPaymentRepository;

    /**
     * 모리캐시 잔액 조회
     */
    public MoriCashBalanceResponseDto getBalance(User user) {
        log.info("모리캐시 잔액 조회 - 사용자: {}", user.getId());

        // 1. 모리캐시 잔액 조회 (없으면 생성)
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseGet(() -> {
                    MoriCashBalance newBalance = MoriCashBalance.createInitialBalance(user);
                    return moriCashBalanceRepository.save(newBalance);
                });

        // 2. 총 충전 금액 계산
        Integer totalCharged = moriCashPaymentRepository.getTotalChargedAmountByUser(user);
        if (totalCharged == null) totalCharged = 0;

        // 3. 총 사용 금액 계산
        Integer totalUsed = moriCashPaymentRepository.getTotalUsedAmountByUser(user);
        if (totalUsed == null) totalUsed = 0;

        MoriCashBalanceResponseDto responseDto = MoriCashBalanceResponseDto.from(balance);
        
        // 총 충전/사용 금액 추가
        responseDto = MoriCashBalanceResponseDto.builder()
                .balanceId(responseDto.getBalanceId())
                .userId(responseDto.getUserId())
                .totalBalance(responseDto.getTotalBalance())
                .availableBalance(responseDto.getAvailableBalance())
                .frozenBalance(responseDto.getFrozenBalance())
                .totalCharged(totalCharged)
                .totalUsed(totalUsed)
                .build();

        log.info("모리캐시 잔액 조회 완료 - 사용자: {}, 총잔액: {}, 사용가능: {}, 동결: {}", 
                user.getId(), balance.getTotalBalance(), balance.getAvailableBalance(), balance.getFrozenBalance());

        return responseDto;
    }

    /**
     * 모리캐시 잔액 이력 조회 (최근 거래 내역)
     */
    public MoriCashBalanceResponseDto getBalanceWithHistory(User user) {
        // 기본 잔액 정보 + 최근 거래 내역 포함
        return getBalance(user);
    }

    /**
     * 모리캐시 거래 내역 조회 (페이징)
     */
    public Page<MoriCashPaymentResponseDto> getHistory(User user, Pageable pageable) {
        log.info("모리캐시 거래 내역 조회 - 사용자: {}", user.getId());
        
        Page<MoriCashPayment> payments = moriCashPaymentRepository.findByUser(user, pageable);
        return payments.map(MoriCashPaymentResponseDto::from);
    }
}
