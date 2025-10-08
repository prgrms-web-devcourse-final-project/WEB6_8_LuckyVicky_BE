package com.back.domain.order.exchange.entity;

import com.back.domain.order.order.entity.Order;
import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeReasonType reasonType; // 교환 사유 타입

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

    @OneToMany(mappedBy = "exchange", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExchangeItem> exchangeItems = new ArrayList<>();

    @Builder
    public Exchange(Order order, User user, ExchangeStatus status, ExchangeReasonType reasonType,
                   String reason, String detailReason, ExchangeMethod exchangeMethod, 
                   String attachmentFiles, String newShippingAddress1, String newShippingAddress2, 
                   String newShippingZip, String newRecipientName, String newRecipientPhone) {
        this.order = order;
        this.user = user;
        this.status = status;
        this.reasonType = reasonType;
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


    // ==================== Factory Methods ====================

    /**
     * 교환 엔티티 생성 팩토리 메서드
     */
    public static Exchange createExchange(Order order, User user, ExchangeReasonType reasonType,
                                        String reason, String detailReason, ExchangeMethod exchangeMethod, 
                                        String attachmentFiles, String newShippingAddress1, 
                                        String newShippingAddress2, String newShippingZip, 
                                        String newRecipientName, String newRecipientPhone) {
        return Exchange.builder()
                .order(order)
                .user(user)
                .status(ExchangeStatus.REQUESTED)
                .reasonType(reasonType)
                .reason(reason)
                .detailReason(detailReason)
                .exchangeMethod(exchangeMethod)
                .attachmentFiles(attachmentFiles)
                .newShippingAddress1(newShippingAddress1)
                .newShippingAddress2(newShippingAddress2)
                .newShippingZip(newShippingZip)
                .newRecipientName(newRecipientName)
                .newRecipientPhone(newRecipientPhone)
                .build();
    }

    // ==================== Business Methods ====================

    /**
     * 교환 승인 처리
     */
    public void approve() {
        this.status = ExchangeStatus.COMPLETED;
    }

    // ==================== Enums ====================

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
