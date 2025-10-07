package com.back.domain.payment.moriCash.entity;

public enum MoriCashPaymentStatus {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"),
    FAILED("결제 실패"),
    CANCELLED("결제 취소");

    private final String description;

    MoriCashPaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
