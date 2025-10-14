package com.back.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 추가/수정 요청 DTO
 * - 일반 장바구니: optionInfo 사용
 * - 펀딩 장바구니: fundingId, fundingPrice, fundingStock 사용
 */
public record CartRequestDto(
    @NotNull(message = "상품 ID는 필수입니다")
    Long productId, // 상품 ID

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    Integer quantity, // 수량

    String optionInfo, // 옵션 정보 (일반 장바구니만 사용)

    @NotNull(message = "장바구니 타입은 필수입니다")
    String cartType, // "NORMAL" 또는 "FUNDING"

    // 펀딩 장바구니 전용 필드
    String fundingId, // 펀딩 고유 ID (펀딩 장바구니만 사용)
    Integer fundingPrice, // 펀딩 단일 가격 (펀딩 장바구니만 사용)
    Integer fundingStock // 펀딩 단일 재고 (펀딩 장바구니만 사용)
) {}
