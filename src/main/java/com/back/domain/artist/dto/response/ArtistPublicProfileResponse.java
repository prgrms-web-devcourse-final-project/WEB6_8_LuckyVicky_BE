package com.back.domain.artist.dto.response;

import com.back.domain.artist.entity.ArtistProfile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
/**
 * 작가 공개 프로필 응답 DTO
 */
@Schema(description = "작가 공개 프로필 정보")
public record ArtistPublicProfileResponse(
        @Schema(description = "작가 ID", example = "42")
        Long artistId,

        @Schema(description = "작가명", example = "김도예")
        String artistName,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "작가 소개", example = "전통 도자기를 현대적으로 재해석하는 작가입니다.")
        String description,

        @Schema(description = "주력 상품", example = "도자기, 머그컵")
        String mainProducts,

        @Schema(description = "SNS 계정", example = "@artist_kim")
        String snsAccount,

        @Schema(description = "팔로워 수", example = "1250")
        Integer followerCount,

        @Schema(description = "총 판매액", example = "5000000")
        Long totalSales,

        @Schema(description = "등록 상품 수", example = "15")
        Integer productCount,

        @Schema(description = "작가 등록일", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    public static ArtistPublicProfileResponse from(ArtistProfile artistProfile) {
        return new ArtistPublicProfileResponse(
                artistProfile.getUser().getId(),
                artistProfile.getArtistName(),
                artistProfile.getProfileImageUrl(),
                artistProfile.getDescription(),
                artistProfile.getMainProducts(),
                artistProfile.getSnsAccount(),
                artistProfile.getFollowerCount(),
                artistProfile.getTotalSales(),
                artistProfile.getProductCount(),
                artistProfile.getCreateDate()
        );
    }
}