package com.back.domain.payment.moriCash.entity;

public enum MoriCashRechargeStatus {
    PENDING("충전 대기"),
    COMPLETED("충전 완료"),
    FAILED("충전 실패"),
    CANCELLED("충전 취소");

    private final String description;

    MoriCashRechargeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
