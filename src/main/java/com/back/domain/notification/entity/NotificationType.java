package com.back.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // 사용자 알림 (7개)
    ORDER_CONFIRMED("주문이 확정되었습니다"),
    SHIPPING_STARTED("상품이 발송되었습니다"),
    DELIVERY_COMPLETED("배송이 완료되었습니다"),
    ORDER_CANCELLED("주문이 취소되었습니다"),
    REFUND_COMPLETED("환불이 완료되었습니다"),
    FUNDING_SUCCESS("참여한 펀딩이 성공했습니다"),
    FUNDING_FAILED("참여한 펀딩이 목표 금액 미달로 종료되었습니다"),
    
    // 작가 알림 (5개)
    NEW_ORDER("새로운 주문이 들어왔습니다"),
    ORDER_CANCELLED_SELLER("주문이 취소되었습니다"),
    FUNDING_SUCCESS_SELLER("펀딩이 목표 금액을 달성했습니다"),
    FUNDING_FAILED_SELLER("펀딩이 목표 금액 미달로 종료되었습니다"),
    LOW_STOCK("재고가 부족합니다"),
    
    // 관리자 알림 (1개)
    ARTIST_VERIFICATION_REQUEST("작가 인증 신청");
    
    private final String defaultMessage;
}
