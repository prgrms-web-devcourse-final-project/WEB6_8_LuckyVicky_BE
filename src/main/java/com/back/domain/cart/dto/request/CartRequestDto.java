package com.back.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 추가/수정 요청 DTO
 */
public record CartRequestDto(
    @NotNull(message = "상품 ID는 필수입니다")
    Long productId, // 상품 ID

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    Integer quantity, // 수량

    String optionInfo, // 옵션 정보 ("옵션 : 없음")

    @NotNull(message = "장바구니 타입은 필수입니다")
    String cartType // "NORMAL" 또는 "FUNDING"
) {}
