package com.back.domain.order.refund.entity;

import com.back.domain.order.order.entity.Order;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refunds")
public class Refund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String detailReason;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundMethod refundMethod;

    @Column(columnDefinition = "TEXT")
    private String attachmentFiles; // 파일명들을 쉼표로 구분하여 저장

    @Builder
    public Refund(Order order, User user, RefundStatus status, String reason, 
                  String detailReason, BigDecimal refundAmount, RefundMethod refundMethod, 
                  String attachmentFiles) {
        this.order = order;
        this.user = user;
        this.status = status;
        this.reason = reason;
        this.detailReason = detailReason;
        this.refundAmount = refundAmount;
        this.refundMethod = refundMethod;
        this.attachmentFiles = attachmentFiles;
    }

    // 환불 승인
    public void approve() {
        this.status = RefundStatus.COMPLETED;
    }

    // 환불 거부 (거부 시에도 COMPLETED로 처리)
    public void reject() {
        this.status = RefundStatus.COMPLETED;
    }

    // 환불 상태 enum
    public enum RefundStatus {
        REQUESTED,  // 환불 신청
        COMPLETED   // 환불 완료
    }

    // 환불 방법 enum
    public enum RefundMethod {
        ORIGINAL_PAYMENT,  // 원 결제수단
        CASH,              // 현금
        BANK_TRANSFER      // 계좌이체
    }
}
