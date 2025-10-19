package com.back.domain.payment.moriCash.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mori_cash_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoriCashBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_balance", nullable = false)
    private Integer totalBalance = 0; // 총 캐시 잔액

    @Column(name = "available_balance", nullable = false)
    private Integer availableBalance = 0; // 사용 가능한 캐시 잔액

    @Column(name = "frozen_balance", nullable = false)
    private Integer frozenBalance = 0; // 동결된 캐시 잔액 (환불 대기 등)

    @Column(name = "total_charged", nullable = false)
    private Integer totalCharged = 0; // 총 충전 금액

    @Column(name = "total_used", nullable = false)
    private Integer totalUsed = 0; // 총 사용 금액

    // 작가 정산 통계 필드 추가
    @Column(name = "total_settlement_sales", nullable = false)
    private Integer totalSettlementSales = 0; // 총 정산 매출 (누적)

    @Column(name = "total_settlement_commission", nullable = false)
    private Integer totalSettlementCommission = 0; // 총 정산 수수료 (누적)

    @Column(name = "total_settlement_net_income", nullable = false)
    private Integer totalSettlementNetIncome = 0; // 총 정산 순수익 (누적)

    @Builder
    private MoriCashBalance(User user, Integer totalBalance, Integer availableBalance, 
                          Integer frozenBalance, Integer totalCharged, Integer totalUsed) {
        this.user = user;
        this.totalBalance = totalBalance != null ? totalBalance : 0;
        this.availableBalance = availableBalance != null ? availableBalance : 0;
        this.frozenBalance = frozenBalance != null ? frozenBalance : 0;
        this.totalCharged = totalCharged != null ? totalCharged : 0;
        this.totalUsed = totalUsed != null ? totalUsed : 0;
        this.totalSettlementSales = 0;
        this.totalSettlementCommission = 0;
        this.totalSettlementNetIncome = 0;
    }

    /**
     * 초기 잔액 생성 팩토리 메서드
     */
    public static MoriCashBalance createInitialBalance(User user) {
        return MoriCashBalance.builder()
                .user(user)
                .totalBalance(0)
                .availableBalance(0)
                .frozenBalance(0)
                .totalCharged(0)
                .totalUsed(0)
                .build();
    }

    /**
     * 캐시 충전
     */
    public void addBalance(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("충전 금액은 양수여야 합니다.");
        this.totalBalance += amount;
        this.availableBalance += amount;
        this.totalCharged += amount;
    }

    /**
     * 캐시 사용 (상품 구매 등)
     */
    public void deductBalance(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("사용 금액은 양수여야 합니다.");
        if (this.availableBalance < amount) throw new IllegalStateException("사용 가능한 캐시가 부족합니다.");
        this.totalBalance -= amount;
        this.availableBalance -= amount;
        this.totalUsed += amount;
    }

    /**
     * 캐시 동결 (환불 대기 등)
     */
    public void freezeBalance(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("동결 금액은 양수여야 합니다.");
        if (this.availableBalance < amount) throw new IllegalStateException("동결할 캐시가 부족합니다.");
        this.availableBalance -= amount;
        this.frozenBalance += amount;
    }

    /**
     * 동결 해제 (환불 완료 또는 취소)
     */
    public void unfreezeBalance(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("해제 금액은 양수여야 합니다.");
        if (this.frozenBalance < amount) throw new IllegalStateException("동결된 캐시가 부족합니다.");
        this.availableBalance += amount;
        this.frozenBalance -= amount;
    }

    /**
     * 동결된 캐시 차감 (환불 처리 완료 후)
     */
    public void deductFrozenBalance(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("차감 금액은 양수여야 합니다.");
        if (this.frozenBalance < amount) throw new IllegalStateException("동결된 캐시가 부족합니다.");
        this.totalBalance -= amount;
        this.frozenBalance -= amount;
    }

    /**
     * 캐시 환불 (사용자에게 캐시 복원)
     */
    public void restoreBalance(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("복원 금액은 양수여야 합니다.");
        this.totalBalance += amount;
        this.availableBalance += amount;
    }

    /**
     * 사용 가능한 캐시 확인
     */
    public boolean hasAvailableBalance(Integer amount) {
        return this.availableBalance >= amount;
    }

    /**
     * 총 잔액 확인
     */
    public boolean hasTotalBalance(Integer amount) {
        return this.totalBalance >= amount;
    }

    /**
     * 잔액 동기화 (totalBalance = availableBalance + frozenBalance)
     */
    public void syncBalance() {
        this.totalBalance = this.availableBalance + this.frozenBalance;
    }

    /**
     * 판매 수익 적립 (작가가 상품을 판매했을 때)
     * 구매자가 작가의 상품을 구매하면 수수료를 제외한 순수익이 모리캐시로 적립됨
     */
    public void addSalesRevenue(Integer netAmount) {
        if (netAmount < 0) throw new IllegalArgumentException("수익 금액은 양수여야 합니다.");
        this.totalBalance += netAmount;
        this.availableBalance += netAmount;
        // totalCharged는 충전한 금액이므로 업데이트하지 않음
    }

    /**
     * 환전 요청 시 모리캐시 차감 및 정산 통계 업데이트 (작가 전용)
     * 
     * 참고: 수수료는 판매 시점에 이미 차감되었으므로, 환전 시에는 추가 수수료 없음
     */
    public void processSettlement(Integer amount) {
        if (amount < 0) throw new IllegalArgumentException("환전 금액은 양수여야 합니다.");
        if (this.availableBalance < amount) {
            throw new IllegalStateException("환전 가능한 모리캐시가 부족합니다.");
        }

        // 모리캐시 차감 (환전하는 금액만큼)
        this.totalBalance -= amount;
        this.availableBalance -= amount;

        // 정산 통계 업데이트 (누적) - 환전한 금액만 기록
        this.totalSettlementSales += amount;
        this.totalSettlementCommission += 0;  // 환전 시 수수료 없음
        this.totalSettlementNetIncome += amount;  // 환전 금액 = 순수익
    }
}
