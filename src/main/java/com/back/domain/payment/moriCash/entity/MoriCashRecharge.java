package com.back.domain.payment.moriCash.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mori_cash_recharges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoriCashRecharge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recharge_amount", nullable = false)
    private Integer rechargeAmount; // 충전 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoriCashRechargeStatus status; // 충전 상태

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 충전 완료 시간

    @Column(name = "payment_method")
    private String paymentMethod; // 결제 수단 (네이버페이, 토스페이 등)

    // PG사 관련 필드들 (CashPayment와 유사)
    @Column(name = "pg_provider")
    private String pgProvider; // TOSS, NAVERPAY 등

    @Column(name = "pg_transaction_id")
    private String pgTransactionId; // PG사에서 발급한 거래 ID

    @Column(name = "pg_approval_number")
    private String pgApprovalNumber; // 승인번호


    @Column(name = "failure_reason")
    private String failureReason; // 실패 사유

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 취소 일시

    @Column(name = "cancellation_reason")
    private String cancellationReason; // 취소 사유

    // 충전 후 잔액 (피그마 대시보드용)
    @Column(name = "balance_after")
    private Integer balanceAfter; // 충전 후 잔액

    @Builder
    private MoriCashRecharge(User user, Integer rechargeAmount, String paymentMethod,
                           String pgProvider, Integer balanceAfter) {
        this.user = user;
        this.rechargeAmount = rechargeAmount;
        this.paymentMethod = paymentMethod;
        this.pgProvider = pgProvider;
        this.balanceAfter = balanceAfter;
        this.status = MoriCashRechargeStatus.PENDING;
    }

    /**
     * 충전 완료 처리 (PG사 승인 후)
     */
    public void completeRecharge(String pgTransactionId, String pgApprovalNumber, Integer balanceAfter) {
        this.status = MoriCashRechargeStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.pgApprovalNumber = pgApprovalNumber;
        this.balanceAfter = balanceAfter;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 충전 실패 처리
     */
    public void failRecharge(String failureReason) {
        this.status = MoriCashRechargeStatus.FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 충전 취소 처리
     */
    public void cancelRecharge(String cancellationReason) {
        this.status = MoriCashRechargeStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 충전 완료 여부 확인
     */
    public boolean isCompleted() {
        return this.status == MoriCashRechargeStatus.COMPLETED;
    }

    /**
     * 충전 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        return this.status == MoriCashRechargeStatus.COMPLETED;
    }
}
