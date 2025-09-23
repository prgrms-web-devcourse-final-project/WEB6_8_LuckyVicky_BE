package com.back.domain.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 장바구니 전체 목록 응답 DTO
 */
@Getter
@Builder
public class CartListResponseDto {

    private List<CartResponseDto> normalCartItems; // 일반 장바구니 상품들
    private List<CartResponseDto> fundingCartItems; // 펀딩 장바구니 상품들
    private Integer totalNormalQuantity; // 일반 상품 총 수량
    private Integer totalFundingQuantity; // 펀딩 상품 총 수량
    private Integer totalNormalAmount; // 일반 상품 총 금액
    private Integer totalFundingAmount; // 펀딩 상품 총 금액
}
