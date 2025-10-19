package com.back.domain.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "사용자 취향 기반 상품 추천 요청")
public record PreferenceRequest(
        
        @Schema(description = "태그별 선호도 점수 (0.0 ~ 1.0)", 
                example = "{\"부드러운\": 0.9, \"실용적인\": 0.8, \"데일리\": 0.7}")
        Map<String, Double> preferences,
        
        @Schema(description = "최소 가격", example = "0")
        Integer minPrice,
        
        @Schema(description = "최대 가격", example = "20000")
        Integer maxPrice,
        
        @Schema(description = "추천 개수", example = "5")
        Integer limit,
        
        @Schema(description = "후보 검색 페이지", example = "0")
        Integer page,
        
        @Schema(description = "후보 검색 사이즈", example = "50")
        Integer size,
        
        @Schema(description = "스펙 선호도 (선택)", 
                example = "{\"연필경도:4B\": 0.6, \"종이_gsm:100\": 0.4}")
        Map<String, Double> specPrefs
) {
}
