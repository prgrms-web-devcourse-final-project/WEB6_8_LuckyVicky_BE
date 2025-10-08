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

    @Builder
    private MoriCashBalance(User user, Integer totalBalance, Integer availableBalance, 
                          Integer frozenBalance, Integer totalCharged, Integer totalUsed) {
        this.user = user;
        this.totalBalance = totalBalance != null ? totalBalance : 0;
        this.availableBalance = availableBalance != null ? availableBalance : 0;
        this.frozenBalance = frozenBalance != null ? frozenBalance : 0;
        this.totalCharged = totalCharged != null ? totalCharged : 0;
        this.totalUsed = totalUsed != null ? totalUsed : 0;
    }

    /**
     * 캐시 충전
     */
    public void charge(Integer amount) {
        this.totalBalance += amount;
        this.availableBalance += amount;
        this.totalCharged += amount;
    }

    /**
     * 캐시 사용
     */
    public void use(Integer amount) {
        if (this.availableBalance < amount) {
            throw new IllegalStateException("사용 가능한 캐시가 부족합니다.");
        }
        this.totalBalance -= amount;
        this.availableBalance -= amount;
        this.totalUsed += amount;
    }

    /**
     * 캐시 동결 (환불 대기 등)
     */
    public void freeze(Integer amount) {
        if (this.availableBalance < amount) {
            throw new IllegalStateException("동결할 수 있는 캐시가 부족합니다.");
        }
        this.availableBalance -= amount;
        this.frozenBalance += amount;
    }

    /**
     * 캐시 동결 해제
     */
    public void unfreeze(Integer amount) {
        if (this.frozenBalance < amount) {
            throw new IllegalStateException("동결된 캐시가 부족합니다.");
        }
        this.frozenBalance -= amount;
        this.availableBalance += amount;
    }

    /**
     * 동결된 캐시 환불
     */
    public void refundFromFrozen(Integer amount) {
        if (this.frozenBalance < amount) {
            throw new IllegalStateException("환불할 수 있는 동결 캐시가 부족합니다.");
        }
        this.frozenBalance -= amount;
        this.totalBalance -= amount;
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
}
