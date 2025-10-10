package com.back.domain.payment.cash.service;

import com.back.domain.payment.cash.dto.request.CashExchangeRequestDto;
import com.back.domain.payment.cash.dto.response.CashExchangeResponseDto;
import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionType;
import com.back.domain.payment.cash.repository.CashTransactionRepository;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CashExchangeService {

    private final CashTransactionRepository cashTransactionRepository;
    private final MoriCashBalanceRepository moriCashBalanceRepository;

    /**
     * 캐시 환전 신청 (작가만 가능)
     */
    public CashExchangeResponseDto createExchangeRequest(CashExchangeRequestDto requestDto, User user) {
        log.info("캐시 환전 신청 - 사용자: {}, 금액: {}", user.getId(), requestDto.getAmount());

        // 1. 사용자 권한 검증 (작가인지 확인)
        if (!user.getRole().name().contains("ARTIST")) {
            throw new IllegalArgumentException("작가만 환전이 가능합니다.");
        }

        // 2. 모리캐시 잔액 확인
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        if (balance.getAvailableBalance() < requestDto.getAmount()) {
            throw new IllegalArgumentException("환전 가능한 모리캐시가 부족합니다.");
        }

        // 3. 캐시 거래 생성
        CashTransaction cashTransaction = CashTransaction.builder()
                .user(user)
                .amount(requestDto.getAmount())
                .transactionType(CashTransactionType.EXCHANGE)
                .paymentMethod("계좌이체")
                .pgProvider("INTERNAL")
                .balanceAfter(balance.getTotalBalance() - requestDto.getAmount())
                .build();

        cashTransaction = cashTransactionRepository.save(cashTransaction);

        // 4. 모리캐시 잔액에서 차감 (동결)
        balance.freezeBalance(requestDto.getAmount());
        moriCashBalanceRepository.save(balance);

        log.info("캐시 환전 신청 완료 - 거래ID: {}, 환전금액: {}", cashTransaction.getId(), requestDto.getAmount());

        return CashExchangeResponseDto.from(cashTransaction);
    }

    /**
     * 환전 승인 처리 (관리자)
     */
    public CashExchangeResponseDto approveExchange(Long transactionId, String pgTransactionId, String pgApprovalNumber) {
        log.info("환전 승인 처리 - 거래ID: {}", transactionId);

        CashTransaction cashTransaction = cashTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("캐시 거래를 찾을 수 없습니다."));

        // 1. 거래 완료 처리
        cashTransaction.completeTransaction(pgTransactionId, pgApprovalNumber, cashTransaction.getBalanceAfter());

        // 2. 모리캐시 잔액에서 실제 차감
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(cashTransaction.getUser())
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        balance.deductFrozenBalance(cashTransaction.getAmount());
        moriCashBalanceRepository.save(balance);

        log.info("환전 승인 완료 - 사용자: {}, 환전금액: {}, 잔액: {}", 
                cashTransaction.getUser().getId(), cashTransaction.getAmount(), balance.getTotalBalance());

        return CashExchangeResponseDto.from(cashTransaction);
    }

    /**
     * 환전 거부 처리
     */
    public void rejectExchange(Long transactionId, String rejectionReason) {
        log.info("환전 거부 처리 - 거래ID: {}, 사유: {}", transactionId, rejectionReason);

        CashTransaction cashTransaction = cashTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("캐시 거래를 찾을 수 없습니다."));

        // 1. 거래 실패 처리
        cashTransaction.failTransaction(rejectionReason);

        // 2. 모리캐시 잔액 동결 해제
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(cashTransaction.getUser())
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        balance.unfreezeBalance(cashTransaction.getAmount());
        moriCashBalanceRepository.save(balance);

        log.info("환전 거부 완료 - 사용자: {}, 환전금액: {}", 
                cashTransaction.getUser().getId(), cashTransaction.getAmount());
    }
}
