package com.back.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "작가 검색 결과 DTO")
public record ArtistSearchResultDto(
        @Schema(description = "작가 ID", example = "1")
        Long artistId,
        @Schema(description = "작가 활동명", example = "김작가")
        String artistName,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl
) {
}