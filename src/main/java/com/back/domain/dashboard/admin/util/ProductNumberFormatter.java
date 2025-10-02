package com.back.domain.dashboard.admin.util;

/**
 * 상품 번호 포맷팅 유틸리티
 * 2025.10.01 생성
 */
public class ProductNumberFormatter {

    /* 상품 번호 ID를 포맷팅하고 있음. Product Entity에 productNumber 컬럼 없어서 추가됨 */
    public static String format(Long productId) {
        if (productId == null) {
            return null;
        }
        // 7자리 숫자로 포맷팅 (예: 1 -> 0000001, 123357 -> 0123357)
        return String.format("%07d", productId);
    }
}
