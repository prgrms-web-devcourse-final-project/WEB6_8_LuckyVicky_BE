package com.back.domain.payment.moriCash.entity;

public enum TransactionType {
    CHARGING("캐시 충전"),
    PURCHASE("상품 구매");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
