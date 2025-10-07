package com.back.domain.payment.moriCash.repository;

import com.back.domain.payment.moriCash.entity.MoriCashPayment;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.order.order.entity.Order;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MoriCashPaymentRepository extends JpaRepository<MoriCashPayment, Long> {

    /**
     * 주문별 모리캐시 결제 조회
     */
    List<MoriCashPayment> findByOrder(Order order);

    /**
     * 사용자별 모리캐시 결제 조회 (페이징)
     */
    Page<MoriCashPayment> findByUserOrderByCreateDateDesc(User user, Pageable pageable);

    /**
     * 결제 상태별 조회 (페이징)
     */
    Page<MoriCashPayment> findByStatusOrderByCreateDateDesc(MoriCashPaymentStatus status, Pageable pageable);

    /**
     * 캐시 거래 ID로 조회
     */
    Optional<MoriCashPayment> findByCashTransactionId(String cashTransactionId);

    /**
     * 포인트 거래 ID로 조회
     */
    Optional<MoriCashPayment> findByPointTransactionId(String pointTransactionId);

    /**
     * 환불 ID로 조회
     */
    Optional<MoriCashPayment> findByRefundId(String refundId);

    /**
     * 특정 기간 내 모리캐시 결제 조회
     */
    @Query("SELECT mcp FROM MoriCashPayment mcp WHERE mcp.createDate BETWEEN :startDate AND :endDate ORDER BY mcp.createDate DESC")
    List<MoriCashPayment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * 주문별 모리캐시 결제 금액 합계
     */
    @Query("SELECT SUM(mcp.totalPrice) FROM MoriCashPayment mcp WHERE mcp.order = :order AND mcp.status = 'COMPLETED'")
    Integer getTotalPaidAmountByOrder(@Param("order") Order order);

    /**
     * 사용자별 모리캐시 결제 금액 합계
     */
    @Query("SELECT SUM(mcp.totalPrice) FROM MoriCashPayment mcp WHERE mcp.user = :user AND mcp.status = 'COMPLETED'")
    Integer getTotalPaidAmountByUser(@Param("user") User user);

    /**
     * 환불 가능한 모리캐시 결제 조회
     */
    @Query("SELECT mcp FROM MoriCashPayment mcp WHERE mcp.status = 'COMPLETED' AND mcp.refundPrice IS NULL")
    List<MoriCashPayment> findRefundableMoriCashPayments();

    /**
     * 주문별 모리캐시 환불 금액 합계
     */
    @Query("SELECT SUM(mcp.refundPrice) FROM MoriCashPayment mcp WHERE mcp.order = :order AND mcp.refundPrice IS NOT NULL")
    Integer getTotalRefundAmountByOrder(@Param("order") Order order);

    /**
     * 결제 방법별 조회
     */
    @Query("SELECT mcp FROM MoriCashPayment mcp WHERE mcp.paymentMethod = :paymentMethod ORDER BY mcp.createDate DESC")
    List<MoriCashPayment> findByPaymentMethod(@Param("paymentMethod") com.back.domain.order.order.entity.PaymentMethod paymentMethod);

    /**
     * 사용자별 모리캐시 사용량 합계
     */
    @Query("SELECT SUM(mcp.usedMoriCash) FROM MoriCashPayment mcp WHERE mcp.user = :user AND mcp.status = 'COMPLETED' AND mcp.usedMoriCash IS NOT NULL")
    Integer getTotalUsedMoriCashByUser(@Param("user") User user);

    /**
     * 사용자별 포인트 사용량 합계
     */
    @Query("SELECT SUM(mcp.usedPoint) FROM MoriCashPayment mcp WHERE mcp.user = :user AND mcp.status = 'COMPLETED' AND mcp.usedPoint IS NOT NULL")
    Integer getTotalUsedPointByUser(@Param("user") User user);
}
