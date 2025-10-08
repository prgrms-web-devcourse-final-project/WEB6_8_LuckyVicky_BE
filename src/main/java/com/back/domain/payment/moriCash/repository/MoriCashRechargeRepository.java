package com.back.domain.payment.moriCash.repository;

import com.back.domain.payment.moriCash.entity.MoriCashRecharge;
import com.back.domain.payment.moriCash.entity.MoriCashRechargeStatus;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MoriCashRechargeRepository extends JpaRepository<MoriCashRecharge, Long> {

    /**
     * 사용자별 캐시 충전 내역 조회 (페이징)
     */
    Page<MoriCashRecharge> findByUserOrderByCreateDateDesc(User user, Pageable pageable);

    /**
     * 충전 상태별 조회 (페이징)
     */
    Page<MoriCashRecharge> findByStatusOrderByCreateDateDesc(MoriCashRechargeStatus status, Pageable pageable);

    /**
     * PG사 거래 ID로 조회
     */
    Optional<MoriCashRecharge> findByPgTransactionId(String pgTransactionId);

    /**
     * 특정 기간 내 캐시 충전 조회
     */
    @Query("SELECT mcr FROM MoriCashRecharge mcr WHERE mcr.createDate BETWEEN :startDate AND :endDate ORDER BY mcr.createDate DESC")
    List<MoriCashRecharge> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자별 총 충전 금액
     */
    @Query("SELECT SUM(mcr.rechargeAmount) FROM MoriCashRecharge mcr WHERE mcr.user = :user AND mcr.status = 'COMPLETED'")
    Integer getTotalRechargedAmountByUser(@Param("user") User user);

    /**
     * 결제 수단별 충전 조회
     */
    @Query("SELECT mcr FROM MoriCashRecharge mcr WHERE mcr.paymentMethod = :paymentMethod ORDER BY mcr.createDate DESC")
    List<MoriCashRecharge> findByPaymentMethod(@Param("paymentMethod") String paymentMethod);

    /**
     * PG사별 충전 조회
     */
    @Query("SELECT mcr FROM MoriCashRecharge mcr WHERE mcr.pgProvider = :pgProvider ORDER BY mcr.createDate DESC")
    List<MoriCashRecharge> findByPgProvider(@Param("pgProvider") String pgProvider);
}
