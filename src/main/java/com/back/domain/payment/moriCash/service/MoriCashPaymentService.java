package com.back.domain.payment.moriCash.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.payment.moriCash.dto.request.MoriCashPaymentRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
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
public class MoriCashPaymentService {

    private final MoriCashPaymentRepository moriCashPaymentRepository;
    private final MoriCashBalanceRepository moriCashBalanceRepository;
    private final OrderRepository orderRepository;

    /**
     * 모리캐시로 주문 결제
     */
    public MoriCashPaymentResponseDto createPayment(MoriCashPaymentRequestDto requestDto, User user) {
        log.info("모리캐시 결제 신청 - 사용자: {}, 주문ID: {}, 결제금액: {}", 
                user.getId(), requestDto.getOrderId(), requestDto.getTotalPrice());

        // 1. 주문 정보 조회
        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 2. 주문 소유자 검증
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 주문만 결제할 수 있습니다.");
        }

        // 3. 모리캐시 잔액 확인
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseGet(() -> MoriCashBalance.createInitialBalance(user));

        if (balance.getAvailableBalance() < requestDto.getUsedMoriCash()) {
            throw new IllegalArgumentException("모리캐시 잔액이 부족합니다.");
        }

        // 4. 모리캐시 결제 생성
        MoriCashPayment payment = MoriCashPayment.builder()
                .order(order)
                .user(user)
                .totalPrice(requestDto.getTotalPrice())
                .usedMoriCash(requestDto.getUsedMoriCash())
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(balance.getTotalBalance() - requestDto.getUsedMoriCash())
                .build();

        payment = moriCashPaymentRepository.save(payment);

        // 5. 모리캐시 잔액 차감
        balance.deductBalance(requestDto.getUsedMoriCash());
        moriCashBalanceRepository.save(balance);

        // 6. 결제 완료 처리
        payment.completePayment("INTERNAL_" + payment.getId());

        log.info("모리캐시 결제 완료 - 결제ID: {}, 주문ID: {}, 사용모리캐시: {}, 잔액: {}", 
                payment.getId(), order.getId(), requestDto.getUsedMoriCash(), balance.getTotalBalance());

        return MoriCashPaymentResponseDto.from(payment);
    }

    /**
     * 모리캐시 결제 취소
     */
    public void cancelPayment(Long paymentId, User user) {
        log.info("모리캐시 결제 취소 - 결제ID: {}, 사용자: {}", paymentId, user.getId());

        MoriCashPayment payment = moriCashPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 1. 결제 소유자 검증
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 결제만 취소할 수 있습니다.");
        }

        // 2. 취소 가능 상태 검증
        if (payment.getStatus() != MoriCashPaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("완료된 결제만 취소할 수 있습니다.");
        }

        // 3. 모리캐시 잔액 복원
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("모리캐시 잔액 정보를 찾을 수 없습니다."));

        balance.restoreBalance(payment.getUsedMoriCash());
        moriCashBalanceRepository.save(balance);

        // 4. 결제 취소 처리
        payment.cancelPayment("사용자 요청에 의한 취소");

        // 5. 주문 상태 복원
        Order order = payment.getOrder();
        order.cancel();

        log.info("모리캐시 결제 취소 완료 - 결제ID: {}, 복원금액: {}, 잔액: {}", 
                paymentId, payment.getUsedMoriCash(), balance.getTotalBalance());
    }

    /**
     * 모리캐시 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public MoriCashPaymentResponseDto getPayment(Long paymentId, User user) {
        MoriCashPayment payment = moriCashPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 결제 소유자 검증
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 결제만 조회할 수 있습니다.");
        }

        return MoriCashPaymentResponseDto.from(payment);
    }

    /**
     * 사용자별 모리캐시 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<MoriCashPaymentResponseDto> getPayments(User user, Pageable pageable) {
        Page<MoriCashPayment> payments = moriCashPaymentRepository.findByUser(user, pageable);
        return payments.map(MoriCashPaymentResponseDto::from);
    }
}
