package com.back.domain.payment.moriCash.entity;

import com.back.domain.order.order.entity.Order;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mori_cash_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoriCashPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoriCashPaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refund_id")
    private String refundId;

    @Column(name = "refund_price")
    private Integer refundPrice;

    // 모리캐시 관련 필드들
    @Column(name = "used_mori_cash", nullable = false)
    private Integer usedMoriCash; // 사용한 모리캐시 금액

    @Column(name = "cash_transaction_id")
    private String cashTransactionId; // 내부 캐시 거래 ID

    @Column(name = "failure_reason")
    private String failureReason; // 실패 사유

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 취소 일시

    @Column(name = "cancellation_reason")
    private String cancellationReason; // 취소 사유

    // 피그마 캐시 내역 대시보드를 위한 추가 필드들
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType; // CHARGING(캐시 충전), PURCHASE(상품 구매)

    @Column(name = "description")
    private String description; // "캐시 충전", "상품 구매" 등

    @Column(name = "balance_after")
    private Integer balanceAfter; // 거래 후 잔액

    @Builder
    private MoriCashPayment(Order order, User user, Integer totalPrice,
                          Integer usedMoriCash, String refundId, Integer refundPrice,
                          TransactionType transactionType, String description, Integer balanceAfter) {
        this.order = order;
        this.user = user;
        this.totalPrice = totalPrice;
        this.usedMoriCash = usedMoriCash;
        this.refundId = refundId;
        this.refundPrice = refundPrice;
        this.transactionType = transactionType;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.status = MoriCashPaymentStatus.PENDING;
    }

    /**
     * 결제 완료 처리 (모리캐시 차감 후)
     */
    public void completePayment(String cashTransactionId) {
        this.status = MoriCashPaymentStatus.COMPLETED;
        this.cashTransactionId = cashTransactionId;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(String failureReason) {
        this.status = MoriCashPaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 결제 취소 처리
     */
    public void cancelPayment(String cancellationReason) {
        this.status = MoriCashPaymentStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 환불 처리 (모리캐시 복원)
     */
    public void processRefund(Integer refundAmount, String refundId) {
        this.refundPrice = refundAmount;
        this.refundId = refundId;
    }

    /**
     * 결제 완료 여부 확인
     */
    public boolean isCompleted() {
        return this.status == MoriCashPaymentStatus.COMPLETED;
    }

    /**
     * 결제 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        return this.status == MoriCashPaymentStatus.COMPLETED;
    }

    /**
     * 환불 가능 여부 확인
     */
    public boolean isRefundable() {
        return this.status == MoriCashPaymentStatus.COMPLETED && this.refundPrice == null;
    }

    /**
     * 모리캐시 사용 여부 확인
     */
    public boolean isMoriCashUsed() {
        return this.usedMoriCash != null && this.usedMoriCash > 0;
    }
}
