package com.back.domain.payment.settlement.repository;

import com.back.domain.payment.settlement.entity.Settlement;
import com.back.domain.payment.settlement.entity.SettlementStatus;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * 작가별 정산 내역 조회
     */
    List<Settlement> findByArtist(User artist, Sort sort);

    /**
     * 상태별 정산 조회
     */
    List<Settlement> findByStatus(SettlementStatus status);

    /**
     * 완료된 정산 중 특정 기간 조회 (관리자 월별 통계용)
     */
    List<Settlement> findByStatusAndCompletedAtBetween(
            SettlementStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 작가별 총 정산 금액 확인 (선택적)
     */
    List<Settlement> findByArtistAndStatus(User artist, SettlementStatus status);
}
