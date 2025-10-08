package com.back.domain.payment.moriCash.repository;

import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoriCashBalanceRepository extends JpaRepository<MoriCashBalance, Long> {

    /**
     * 사용자별 캐시 잔액 조회
     */
    Optional<MoriCashBalance> findByUser(User user);

    /**
     * 사용자 ID로 캐시 잔액 조회
     */
    Optional<MoriCashBalance> findByUserId(Long userId);
}
