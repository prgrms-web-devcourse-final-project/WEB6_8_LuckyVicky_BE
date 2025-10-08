package com.back.domain.payment.cash.entity;

public enum CashTransactionType {
    CHARGING("캐시 충전"),
    EXCHANGE("캐시 환전");

    private final String description;

    CashTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
