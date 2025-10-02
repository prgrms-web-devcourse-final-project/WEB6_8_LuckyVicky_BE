package com.back.domain.order.exchange.entity;

import com.back.domain.order.order.entity.Order;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exchanges")
public class Exchange extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String detailReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeMethod exchangeMethod;

    @Column(columnDefinition = "TEXT")
    private String attachmentFiles; // 파일명들을 쉼표로 구분하여 저장

    // 교환 시 새 배송지 정보
    @Column(length = 500)
    private String newShippingAddress1;

    @Column(length = 500)
    private String newShippingAddress2;

    @Column(length = 10)
    private String newShippingZip;

    @Column(length = 100)
    private String newRecipientName;

    @Column(length = 20)
    private String newRecipientPhone;

    @Builder
    public Exchange(Order order, User user, ExchangeStatus status, String reason, 
                   String detailReason, ExchangeMethod exchangeMethod, String attachmentFiles,
                   String newShippingAddress1, String newShippingAddress2, String newShippingZip,
                   String newRecipientName, String newRecipientPhone) {
        this.order = order;
        this.user = user;
        this.status = status;
        this.reason = reason;
        this.detailReason = detailReason;
        this.exchangeMethod = exchangeMethod;
        this.attachmentFiles = attachmentFiles;
        this.newShippingAddress1 = newShippingAddress1;
        this.newShippingAddress2 = newShippingAddress2;
        this.newShippingZip = newShippingZip;
        this.newRecipientName = newRecipientName;
        this.newRecipientPhone = newRecipientPhone;
    }

    // 교환 승인
    public void approve() {
        this.status = ExchangeStatus.COMPLETED;
    }

    // 교환 거부 (거부 시에도 COMPLETED로 처리)
    public void reject() {
        this.status = ExchangeStatus.COMPLETED;
    }

    // 교환 상태 enum
    public enum ExchangeStatus {
        REQUESTED,  // 교환 신청
        COMPLETED   // 교환 완료
    }

    // 교환 방법 enum
    public enum ExchangeMethod {
        PICKUP,     // 수거 후 교환
        DIRECT      // 직접 교환
    }
}
