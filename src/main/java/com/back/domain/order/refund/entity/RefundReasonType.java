package com.back.domain.order.refund.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 환불 사유 타입
 */
@Getter
@RequiredArgsConstructor
public enum RefundReasonType {
    CHANGE_OF_MIND("단순 변심", true),
    SIZE_COLOR("사이즈/색상 불만", true),
    DEFECTIVE("상품 불량/파손", false),
    DAMAGED_DELIVERY("배송 중 훼손", false),
    WRONG_PRODUCT("오배송", false);

    private final String description;
    private final boolean restoreStock; // 재고 복원 여부

    /**
     * 재고 복원 여부 확인
     */
    public boolean shouldRestoreStock() {
        return this.restoreStock;
    }
}

