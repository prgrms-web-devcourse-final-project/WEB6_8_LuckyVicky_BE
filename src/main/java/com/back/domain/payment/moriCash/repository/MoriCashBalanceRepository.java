package com.back.domain.payment.moriCash.repository;

import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MoriCashBalanceRepository extends JpaRepository<MoriCashBalance, Long> {

    /**
     * 사용자별 캐시 잔액 조회
     */
    Optional<MoriCashBalance> findByUser(User user);
    
    /**
     * 사용자별 캐시 잔액 조회 (Pessimistic Write Lock)
     * 결제/충전/환전 시 사용 - 동시성 제어
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MoriCashBalance m WHERE m.user = :user")
    Optional<MoriCashBalance> findByUserWithLock(@Param("user") User user);

    /**
     * 사용자 ID로 캐시 잔액 조회
     */
    Optional<MoriCashBalance> findByUserId(Long userId);
}
