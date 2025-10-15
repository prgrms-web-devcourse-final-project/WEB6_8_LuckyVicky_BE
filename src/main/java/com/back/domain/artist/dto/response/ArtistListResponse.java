package com.back.domain.artist.dto.response;

import com.back.domain.artist.entity.ArtistProfile;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 작가 목록 항목 DTO (간소화 버전)
 */
@Schema(description = "작가 목록 항목")
public record ArtistListResponse (
    @Schema(description = "작가 ID", example = "42")
    Long artistId,

    @Schema(description = "작가명", example = "김도예")
    String artistName
) {
        public static ArtistListResponse from(ArtistProfile artistProfile) {
            return new ArtistListResponse(
                    artistProfile.getId(),
                    artistProfile.getArtistName()
            );
        }
    }