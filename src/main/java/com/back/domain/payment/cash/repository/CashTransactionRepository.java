package com.back.domain.payment.cash.repository;

import com.back.domain.payment.cash.entity.CashTransaction;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import com.back.domain.payment.cash.entity.CashTransactionType;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {

    /**
     * 사용자별 현금 거래 조회 (페이징)
     */
    Page<CashTransaction> findByUserOrderByCreateDateDesc(User user, Pageable pageable);

    /**
     * 거래 타입별 조회 (페이징)
     */
    Page<CashTransaction> findByTransactionTypeOrderByCreateDateDesc(CashTransactionType transactionType, Pageable pageable);

    /**
     * 거래 상태별 조회 (페이징)
     */
    Page<CashTransaction> findByStatusOrderByCreateDateDesc(CashTransactionStatus status, Pageable pageable);

    /**
     * PG사 거래 ID로 조회
     */
    Optional<CashTransaction> findByPgTransactionId(String pgTransactionId);

    /**
     * 특정 기간 내 현금 거래 조회
     */
    @Query("SELECT ct FROM CashTransaction ct WHERE ct.createDate BETWEEN :startDate AND :endDate ORDER BY ct.createDate DESC")
    List<CashTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자별 총 충전 금액
     */
    @Query("SELECT SUM(ct.amount) FROM CashTransaction ct WHERE ct.user = :user AND ct.transactionType = 'CHARGING' AND ct.status = 'COMPLETED'")
    Integer getTotalChargedAmountByUser(@Param("user") User user);

    /**
     * 사용자별 총 환전 금액
     */
    @Query("SELECT SUM(ct.amount) FROM CashTransaction ct WHERE ct.user = :user AND ct.transactionType = 'EXCHANGE' AND ct.status = 'COMPLETED'")
    Integer getTotalExchangedAmountByUser(@Param("user") User user);

    /**
     * 거래 타입과 상태별 조회
     */
    @Query("SELECT ct FROM CashTransaction ct WHERE ct.transactionType = :transactionType AND ct.status = :status ORDER BY ct.createDate DESC")
    List<CashTransaction> findByTransactionTypeAndStatus(@Param("transactionType") CashTransactionType transactionType, 
                                                         @Param("status") CashTransactionStatus status);
}
