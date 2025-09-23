package com.back.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 추가/수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class CartRequestDto {

    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId; // 상품 ID

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity; // 수량

    private String optionInfo; // 옵션 정보 ("옵션 : 없음")

    @NotNull(message = "장바구니 타입은 필수입니다")
    private String cartType; // "NORMAL" 또는 "FUNDING"

    @Builder
    public CartRequestDto(Long productId, Integer quantity, String optionInfo, String cartType) {
        this.productId = productId;
        this.quantity = quantity;
        this.optionInfo = optionInfo;
        this.cartType = cartType;
    }
}
