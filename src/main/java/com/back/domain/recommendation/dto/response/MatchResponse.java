package com.back.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "취향 매칭 추천 응답")
public record MatchResponse(
        @Schema(description = "추천 상품 목록")
        List<RecommendedItem> recommendations
) {
}
