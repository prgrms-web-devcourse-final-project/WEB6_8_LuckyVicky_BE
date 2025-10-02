package com.back.domain.order.order.entity;

public enum OrderStatus {
    PAYMENT_COMPLETED,  // 결제완료
    PREPARING_SHIPMENT, // 배송준비중
    SHIPPING,          // 배송중
    DELIVERED,         // 배송완료
    CANCELLED,         // 주문 취소
    REFUNDED,          // 환불 완료
    REFUND_REQUESTED,  // 환불 신청
    EXCHANGE_REQUESTED // 교환 신청
}