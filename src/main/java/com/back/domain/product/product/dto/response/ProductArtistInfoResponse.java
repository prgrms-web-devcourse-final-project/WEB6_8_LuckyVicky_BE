package com.back.domain.product.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 상품 상세 - 작가 정보 조회 응답 DTO
 */
@Schema(name = "ProductArtistInfoResponse", description = "상품 상세 페이지에서 작가 정보 조회 응답 DTO")
public record ProductArtistInfoResponse(
        @Schema(description = "작가명", example = "김작가")
        String artistName,

        @Schema(description = "팔로워 수", example = "350")
        Integer followerCount,

        @Schema(description = "작가 승인일", example = "2025.10.10")
        String approvedDate,

        @Schema(description = "작가 프로필 이미지 URL", example = "https://s3.mori-mori.store/profiles/abc.jpg")
        String profileImageUrl,

        @Schema(description = "작가 페이지 URL", example = "https://www.mori-mori.store/artist/123")
        String artistPageUrl,

        @Schema(description = "작가 소개글", example = "저는 일러스트 전문 작가 김작가입니다...")
        String description
) {}
