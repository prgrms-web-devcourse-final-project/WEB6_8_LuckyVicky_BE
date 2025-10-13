package com.back.domain.payment.settlement.entity;

public enum SettlementStatus {
    PENDING("미체결"),
    COMPLETED("정산완료"),
    REJECTED("거부됨");

    private final String description;

    SettlementStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
