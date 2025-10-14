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
     * 작가별 총 환전 금액 (작가만 환전 가능)
     */
    @Query("SELECT SUM(ct.amount) FROM CashTransaction ct WHERE ct.user = :user AND ct.transactionType = 'EXCHANGE' AND ct.status = 'COMPLETED'")
    Integer getTotalExchangedAmountByUser(@Param("user") User user);

    /**
     * 거래 타입과 상태별 조회
     */
    @Query("SELECT ct FROM CashTransaction ct WHERE ct.transactionType = :transactionType AND ct.status = :status ORDER BY ct.createDate DESC")
    List<CashTransaction> findByTransactionTypeAndStatus(@Param("transactionType") CashTransactionType transactionType, 
                                                         @Param("status") CashTransactionStatus status);

    /**
     * 사용자별 충전 내역 조회 (COMPLETED만)
     */
    @Query("SELECT ct FROM CashTransaction ct WHERE ct.user = :user " +
           "AND ct.transactionType = 'CHARGING' " +
           "AND ct.status = 'COMPLETED' " +
           "ORDER BY ct.completedAt DESC, ct.createDate DESC")
    List<CashTransaction> findCompletedChargingByUser(@Param("user") User user);

    /**
     * 작가별 캐시 거래 내역 조회 (페이징, 다중 조건 필터링, 동적 정렬)
     * @param user 사용자
     * @param transactionType 거래 유형 (CHARGING, EXCHANGE)
     * @param status 거래 상태 (PENDING, COMPLETED, FAILED, CANCELLED)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 및 정렬 정보
     * @return 조회된 거래 내역 페이지
     */
    @Query("SELECT ct FROM CashTransaction ct " +
           "WHERE ct.user = :user " +
           "AND (:transactionType IS NULL OR ct.transactionType = :transactionType) " +
           "AND (:status IS NULL OR ct.status = :status) " +
           "AND (:startDate IS NULL OR ct.createDate >= :startDate) " +
           "AND (:endDate IS NULL OR ct.createDate <= :endDate)")
    Page<CashTransaction> findCashTransactionsByUserWithFilters(
            @Param("user") User user,
            @Param("transactionType") CashTransactionType transactionType,
            @Param("status") CashTransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 기간 내 사용자별 입금(충전) 합계
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct " +
           "WHERE ct.user = :user " +
           "AND ct.transactionType = 'CHARGING' " +
           "AND ct.status = 'COMPLETED' " +
           "AND (:startDate IS NULL OR ct.completedAt >= :startDate) " +
           "AND (:endDate IS NULL OR ct.completedAt <= :endDate)")
    Integer getPeriodDepositTotal(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간 내 사용자별 환전 합계
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct " +
           "WHERE ct.user = :user " +
           "AND ct.transactionType = 'EXCHANGE' " +
           "AND ct.status = 'COMPLETED' " +
           "AND (:startDate IS NULL OR ct.completedAt >= :startDate) " +
           "AND (:endDate IS NULL OR ct.completedAt <= :endDate)")
    Integer getPeriodWithdrawalTotal(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
