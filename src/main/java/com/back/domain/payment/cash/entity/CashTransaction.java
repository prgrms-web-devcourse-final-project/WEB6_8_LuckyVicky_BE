package com.back.domain.payment.cash.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cash_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashTransactionType transactionType; // CHARGING(충전), EXCHANGE(환전)

    @Column(name = "amount", nullable = false)
    private Integer amount; // 충전/환전 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashTransactionStatus status; // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 완료 시간

    // PG사 관련 필드들 (토스페이먼츠)
    @Column(name = "pg_provider")
    private String pgProvider; // "TOSS"

    @Column(name = "pg_transaction_id")
    private String pgTransactionId; // 토스 거래 ID

    @Column(name = "pg_approval_number")
    private String pgApprovalNumber; // 토스 승인번호

    @Column(name = "failure_reason")
    private String failureReason; // 실패 사유

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 취소 일시

    @Column(name = "cancellation_reason")
    private String cancellationReason; // 취소 사유

    // 캐시 잔액 관련 (피그마 대시보드용)
    @Column(name = "balance_after")
    private Integer balanceAfter; // 거래 후 캐시 잔액

    @Builder
    private CashTransaction(User user, CashTransactionType transactionType, Integer amount,
                          String pgProvider, Integer balanceAfter) {
        this.user = user;
        this.transactionType = transactionType;
        this.amount = amount;
        this.pgProvider = pgProvider;
        this.balanceAfter = balanceAfter;
        this.status = CashTransactionStatus.PENDING;
    }

    /**
     * 거래 완료 처리 (PG사 승인 후)
     */
    public void completeTransaction(String pgTransactionId, String pgApprovalNumber, Integer balanceAfter) {
        this.status = CashTransactionStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.pgApprovalNumber = pgApprovalNumber;
        this.balanceAfter = balanceAfter;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 거래 실패 처리
     */
    public void failTransaction(String failureReason) {
        this.status = CashTransactionStatus.FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 거래 취소 처리
     */
    public void cancelTransaction(String cancellationReason) {
        this.status = CashTransactionStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 거래 완료 여부 확인
     */
    public boolean isCompleted() {
        return this.status == CashTransactionStatus.COMPLETED;
    }

    /**
     * 거래 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        return this.status == CashTransactionStatus.COMPLETED;
    }

    /**
     * 충전 거래인지 확인
     */
    public boolean isCharging() {
        return this.transactionType == CashTransactionType.CHARGING;
    }

    /**
     * 환전 거래인지 확인
     */
    public boolean isExchange() {
        return this.transactionType == CashTransactionType.EXCHANGE;
    }
}
