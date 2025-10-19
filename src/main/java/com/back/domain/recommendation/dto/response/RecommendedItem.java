package com.back.domain.recommendation.dto.response;

import com.back.domain.product.product.dto.response.ProductListResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "추천 상품 아이템")
public record RecommendedItem(
        @Schema(description = "추천 순위", example = "1")
        int rank,
        
        @Schema(description = "매칭 점수", example = "2.4")
        double matchScore,
        
        @Schema(description = "상품 정보")
        ProductListResponse.ProductInfo product,
        
        @Schema(description = "매칭된 태그 목록")
        List<MatchedTag> matchedTags,
        
        @Schema(description = "추천 이유", example = "'부드러운·실용적인·데일리' 선호와 일치")
        String reason
) {
}
