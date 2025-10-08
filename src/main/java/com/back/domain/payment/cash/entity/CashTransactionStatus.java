package com.back.domain.payment.cash.entity;

public enum CashTransactionStatus {
    PENDING("처리 대기"),
    COMPLETED("처리 완료"),
    FAILED("처리 실패"),
    CANCELLED("처리 취소");

    private final String description;

    CashTransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
