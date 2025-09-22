package com.back.domain.product.product.entity;

// 판매 상태
public enum SellingStatus {
    BEFORE_SELLING, //판매 전
    SELLING, // 판매 중
    SOLD_OUT, // 품절
    END_OF_SALE // 판매 종료
}
