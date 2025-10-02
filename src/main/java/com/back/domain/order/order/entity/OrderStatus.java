package com.back.domain.order.order.entity;

public enum OrderStatus {
    PAYMENT_COMPLETED,        // 결제완료 (발주 전)
    PREPARING_SHIPMENT,       // 배송 준비중
    SHIPPING,                 // 배송중
    DELIVERED,                // 배송완료
    
    // 취소 관련
    CANCELLATION_REQUESTED,   // 취소 신청
    CANCELLATION_COMPLETED,   // 취소 완료
    
    // 교환 관련
    EXCHANGE_REQUESTED,       // 교환 신청
    EXCHANGE_COMPLETED,       // 교환 완료
    
    // 환불 관련
    REFUND_REQUESTED,         // 환불 신청
    REFUND_COMPLETED          // 환불 완료
}