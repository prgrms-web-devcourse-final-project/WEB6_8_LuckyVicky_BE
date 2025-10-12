package com.back.domain.support.inquiry.entity;

/**
 * 문의 카테고리
 */
public enum InquiryCategory {
    PRODUCT("상품"),
    DELIVERY("배송"),
    RESTOCK("입고/재입고"),
    EXCHANGE_REFUND("교환/환불"),
    ARTIST("작가입점"),
    QUALITY("품질/불량"),
    ACCOUNT("계정"),
    PAYMENT("결제"),
    ETC("기타");

    private final String description;

    InquiryCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
