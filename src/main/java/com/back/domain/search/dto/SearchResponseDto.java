package com.back.domain.search.dto;

import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.product.product.dto.response.ProductListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "통합 검색 결과 응답 DTO")
public record SearchResponseDto(
        @Schema(description = "상품 검색 결과 목록")
        List<ProductListResponse.ProductInfo> products,
        @Schema(description = "작가 검색 결과 목록")
        List<ArtistSearchResultDto> artists,
        @Schema(description = "펀딩 검색 결과 목록")
        List<FundingCardDto> fundings
) {
}
