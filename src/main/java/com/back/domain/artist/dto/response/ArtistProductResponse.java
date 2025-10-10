package com.back.domain.artist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * 작가 상품 응답 DTO
 */
@Schema(description = "작가 상품 정보")
public record ArtistProductResponse(
        @Schema(description = "상품 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID productUuid,

        @Schema(description = "상품명", example = "핸드메이드 머그컵")
        String name,

        @Schema(description = "정가", example = "25000")
        Integer price,

        @Schema(description = "할인가", example = "20000")
        Integer discountPrice,

        @Schema(description = "할인율", example = "20")
        Integer discountRate,

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
        String thumbnailUrl,

        @Schema(description = "평균 평점", example = "4.5")
        Double rating,

        @Schema(description = "리뷰 수", example = "35")
        Long reviewCount,

        @Schema(description = "재고 수량", example = "10")
        Integer stock,

        @Schema(description = "판매 상태", example = "SELLING")
        String sellingStatus
) {
}