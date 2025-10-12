package com.back.domain.payment.moriCash.service;

import com.back.domain.payment.moriCash.dto.request.MoriCashRefundRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashRefundResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.entity.MoriCashPayment;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.payment.moriCash.entity.TransactionType;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.payment.moriCash.repository.MoriCashPaymentRepository;
import com.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MoriCashRefundService {

    private final MoriCashPaymentRepository moriCashPaymentRepository;
    private final MoriCashBalanceRepository moriCashBalanceRepository;

    /**
     * 모리캐시 환불 처리
     */
    public MoriCashRefundResponseDto processRefund(MoriCashRefundRequestDto requestDto, User user) {
        log.info("모리캐시 환불 처리 - 결제ID: {}, 환불금액: {}, 환불사유: {}", 
                requestDto.getPaymentId(), requestDto.getRefundAmount(), requestDto.getRefundReason());

        // 1. 결제 정보 조회
        MoriCashPayment payment = moriCashPaymentRepository.findById(requestDto.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 2. 결제 소유자 검증
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 결제만 환불할 수 있습니다.");
        }

        // 3. 환불 가능 상태 검증
        if (payment.getStatus() != MoriCashPaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("완료된 결제만 환불할 수 있습니다.");
        }

        // 4. 환불 금액 검증
        if (requestDto.getRefundAmount() <= 0 || requestDto.getRefundAmount() > payment.getUsedMoriCash()) {
            throw new IllegalArgumentException("환불 금액이 올바르지 않습니다.");
        }

        // 5. 모리캐시 잔액 복원
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        balance.restoreBalance(requestDto.getRefundAmount());
        moriCashBalanceRepository.save(balance);

        // 6. 환불 처리
        payment.processRefund("REFUND_" + System.currentTimeMillis(), requestDto.getRefundAmount());

        log.info("모리캐시 환불 완료 - 결제ID: {}, 환불금액: {}, 복원후 잔액: {}", 
                payment.getId(), requestDto.getRefundAmount(), balance.getTotalBalance());

        return MoriCashRefundResponseDto.from(payment, balance.getTotalBalance());
    }

    /**
     * 모리캐시 환불 취소 (관리자)
     */
    public void cancelRefund(Long paymentId, String cancellationReason) {
        log.info("모리캐시 환불 취소 - 결제ID: {}, 취소사유: {}", paymentId, cancellationReason);

        MoriCashPayment payment = moriCashPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 1. 환불 상태 검증
        if (payment.getRefundPrice() == null || payment.getRefundPrice() <= 0) {
            throw new IllegalArgumentException("환불 처리된 결제가 아닙니다.");
        }

        // 2. 모리캐시 잔액에서 환불 금액 차감
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(payment.getUser())
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        balance.deductBalance(payment.getRefundPrice());
        moriCashBalanceRepository.save(balance);

        // 3. 환불 취소 처리
        payment.cancelRefund(cancellationReason);

        log.info("모리캐시 환불 취소 완료 - 결제ID: {}, 차감금액: {}, 잔액: {}", 
                paymentId, payment.getRefundPrice(), balance.getTotalBalance());
    }

    /**
     * 모리캐시 환불 상세 조회
     */
    @Transactional(readOnly = true)
    public MoriCashRefundResponseDto getRefund(Long paymentId, User user) {
        MoriCashPayment payment = moriCashPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 결제 소유자 검증
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 결제만 조회할 수 있습니다.");
        }

        // 현재 잔액 조회
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        return MoriCashRefundResponseDto.from(payment, balance.getTotalBalance());
    }

    /**
     * 사용자별 모리캐시 환불 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<MoriCashRefundResponseDto> getRefunds(User user, Pageable pageable) {
        Page<MoriCashPayment> payments = moriCashPaymentRepository.findByUserAndRefundIdIsNotNull(user, pageable);
        
        // 현재 잔액 조회
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));
        
        return payments.map(payment -> MoriCashRefundResponseDto.from(payment, balance.getTotalBalance()));
    }
}
