package com.back.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 추가/수정 요청 DTO
 * - 일반 장바구니: productId, optionInfo 사용
 * - 펀딩 장바구니: fundingId, fundingPrice, fundingStock 사용
 */
@Schema(description = "장바구니 추가 요청")
public record CartRequestDto(
    @Schema(description = "상품 ID (일반 장바구니만 필수)", example = "123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long productId, // 상품 ID (일반 장바구니만 사용)

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "수량", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantity, // 수량

    @Schema(description = "옵션 정보 (일반 장바구니만 사용)", example = "색상: 화이트", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String optionInfo, // 옵션 정보 (일반 장바구니만 사용)

    @NotNull(message = "장바구니 타입은 필수입니다")
    @Schema(description = "장바구니 타입", example = "NORMAL", allowableValues = {"NORMAL", "FUNDING"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String cartType, // "NORMAL" 또는 "FUNDING"

    // 펀딩 장바구니 전용 필드
    @Schema(description = "펀딩 ID (펀딩 장바구니만 필수)", example = "456", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long fundingId, // 펀딩 ID (펀딩 장바구니만 사용)
    
    @Schema(description = "펀딩 가격 (펀딩 장바구니만 사용)", example = "50000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer fundingPrice, // 펀딩 단일 가격 (펀딩 장바구니만 사용)
    
    @Schema(description = "펀딩 재고 (펀딩 장바구니만 사용)", example = "100", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer fundingStock // 펀딩 단일 재고 (펀딩 장바구니만 사용)
) {
    /**
     * 유효성 검증
     */
    public void validate() {
        if ("NORMAL".equals(cartType) && productId == null) {
            throw new IllegalArgumentException("일반 장바구니는 productId가 필수입니다.");
        }
        if ("FUNDING".equals(cartType) && fundingId == null) {
            throw new IllegalArgumentException("펀딩 장바구니는 fundingId가 필수입니다.");
        }
    }
}
