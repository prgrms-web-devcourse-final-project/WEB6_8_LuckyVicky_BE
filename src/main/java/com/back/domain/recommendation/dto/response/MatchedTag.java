package com.back.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매칭된 태그 정보")
public record MatchedTag(
        @Schema(description = "태그명", example = "부드러운")
        String name,
        
        @Schema(description = "사용자 선호도 점수", example = "0.9")
        double yourScore
) {
}
