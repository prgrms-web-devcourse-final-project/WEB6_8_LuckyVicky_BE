package com.back.domain.payment.cash.service;

import com.back.domain.payment.cash.dto.request.CashChargeRequestDto;
import com.back.domain.payment.cash.dto.response.CashChargeResponseDto;
import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
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
public class CashChargeService {

    private final CashTransactionRepository cashTransactionRepository;
    private final MoriCashBalanceRepository moriCashBalanceRepository;

    /**
     * 캐시 충전 신청
     */
    public CashChargeResponseDto createChargeRequest(CashChargeRequestDto requestDto, User user) {
        log.info("캐시 충전 신청 - 사용자: {}, 금액: {}", user.getId(), requestDto.getAmount());

        // 1. 캐시 거래 생성
        CashTransaction cashTransaction = CashTransaction.builder()
                .user(user)
                .amount(requestDto.getAmount())
                .transactionType(CashTransactionType.CHARGING)
                .paymentMethod(requestDto.getPaymentMethod())
                .pgProvider(requestDto.getPgProvider())
                .build();

        cashTransaction = cashTransactionRepository.save(cashTransaction);

        log.info("캐시 충전 거래 생성 완료 - 거래ID: {}", cashTransaction.getId());

        return CashChargeResponseDto.from(cashTransaction);
    }

    /**
     * PG사 승인 후 캐시 충전 완료 처리
     */
    public CashChargeResponseDto completeCharge(Long transactionId, String pgTransactionId, String pgApprovalNumber) {
        log.info("캐시 충전 완료 처리 - 거래ID: {}, PG거래ID: {}", transactionId, pgTransactionId);

        CashTransaction cashTransaction = cashTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("캐시 거래를 찾을 수 없습니다."));

        // 1. 모리캐시 잔액 업데이트
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(cashTransaction.getUser())
                .orElseGet(() -> MoriCashBalance.createInitialBalance(cashTransaction.getUser()));

        balance.addBalance(cashTransaction.getAmount());
        moriCashBalanceRepository.save(balance);

        // 2. 거래 완료 처리 (잔액 포함)
        cashTransaction.completeTransaction(pgTransactionId, pgApprovalNumber, balance.getTotalBalance());

        log.info("캐시 충전 완료 - 사용자: {}, 충전금액: {}, 잔액: {}", 
                cashTransaction.getUser().getId(), cashTransaction.getAmount(), balance.getTotalBalance());

        return CashChargeResponseDto.from(cashTransaction);
    }

    /**
     * 캐시 충전 실패 처리
     */
    public void failCharge(Long transactionId, String failureReason) {
        log.info("캐시 충전 실패 처리 - 거래ID: {}, 사유: {}", transactionId, failureReason);

        CashTransaction cashTransaction = cashTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("캐시 거래를 찾을 수 없습니다."));

        cashTransaction.failTransaction(failureReason);
    }

    /**
     * 캐시 충전 취소
     */
    public void cancelCharge(Long transactionId, String cancellationReason) {
        log.info("캐시 충전 취소 - 거래ID: {}, 사유: {}", transactionId, cancellationReason);

        CashTransaction cashTransaction = cashTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("캐시 거래를 찾을 수 없습니다."));

        cashTransaction.cancelTransaction(cancellationReason);
    }
}
