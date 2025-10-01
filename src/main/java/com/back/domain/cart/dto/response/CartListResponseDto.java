package com.back.domain.cart.dto.response;

import java.util.List;

/**
 * 장바구니 전체 목록 응답 DTO
 */
public record CartListResponseDto(
    List<CartResponseDto> normalCartItems, // 일반 장바구니 상품들
    List<CartResponseDto> fundingCartItems, // 펀딩 장바구니 상품들
    Integer totalNormalQuantity, // 일반 상품 총 수량
    Integer totalFundingQuantity, // 펀딩 상품 총 수량
    Integer totalNormalAmount, // 일반 상품 총 금액
    Integer totalFundingAmount // 펀딩 상품 총 금액
) {}
