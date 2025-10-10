package com.back.domain.support.faq.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FaqCategory {
    ACCOUNT("회원/가입", "회원가입 및 계정 관련"),
    ORDER_PAYMENT("주문/결제", "주문 및 결제 관련"),
    DELIVERY("배송", "배송 관련"),
    EXCHANGE_RETURN("교환/환불", "교환 및 환불 관련"),
    PRODUCT("상품", "상품 관련"),
    FUNDING("펀딩", "펀딩 관련"),
    ARTIST("작가", "작가 관련"),
    SERVICE("서비스 이용", "사이트 이용방법 및 기능 안내"),
    ETC("기타", "기타 문의");

    private final String displayName;
    private final String description;
}