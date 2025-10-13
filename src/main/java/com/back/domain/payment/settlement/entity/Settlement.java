package com.back.domain.payment.settlement.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @Column(name = "requested_amount", nullable = false)
    private Integer requestedAmount; // 환전 신청 금액 (총매출)

    @Column(name = "commission_rate", nullable = false)
    private Integer commissionRate = 10; // 수수료율 (10%)

    @Column(name = "commission_amount", nullable = false)
    private Integer commissionAmount; // 수수료 금액

    @Column(name = "net_amount", nullable = false)
    private Integer netAmount; // 실제 지급 금액 (순수익)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_holder")
    private String accountHolder;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Builder
    private Settlement(User artist, Integer requestedAmount, Integer commissionRate,
                      String bankName, String accountNumber, String accountHolder) {
        this.artist = artist;
        this.requestedAmount = requestedAmount;
        this.commissionRate = commissionRate != null ? commissionRate : 10;
        this.commissionAmount = calculateCommission(requestedAmount, this.commissionRate);
        this.netAmount = requestedAmount - this.commissionAmount;
        this.status = SettlementStatus.COMPLETED; // 즉시 완료 처리
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.completedAt = LocalDateTime.now(); // 생성 즉시 완료 시간 기록
    }

    /**
     * 수수료 계산
     */
    private Integer calculateCommission(Integer amount, Integer rate) {
        return (int) (amount * (rate / 100.0));
    }

    /**
     * 정산 완료 처리
     */
    public void completeSettlement() {
        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 정산 보류 처리
     */
    public void pendSettlement() {
        this.status = SettlementStatus.PENDING;
    }

    /**
     * 정산 거부 처리
     */
    public void rejectSettlement(String reason) {
        this.status = SettlementStatus.REJECTED;
        this.rejectionReason = reason;
    }

    /**
     * 완료 여부 확인
     */
    public boolean isCompleted() {
        return this.status == SettlementStatus.COMPLETED;
    }
}
