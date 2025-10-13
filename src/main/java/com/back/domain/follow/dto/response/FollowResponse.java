package com.back.domain.follow.dto.response;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.follow.entity.Follow;

import java.time.LocalDateTime;

/**
 * 팔로우 응답 DTO - 팔로우/언팔로우 성공 시 반환
 */
public record FollowResponse(
        Long followId,
        Long artistId,
        String artistName,
        String profileImageUrl,
        Integer followerCount,
        LocalDateTime followedAt,
        boolean isFollowing
) {
    /**
     * Follow 엔티티 -> DTO 변환
     */
    public static FollowResponse from(Follow follow, ArtistProfile artist) {
        return new FollowResponse(
                follow.getId(),
                artist.getId(),
                artist.getArtistName(),
                artist.getProfileImageUrl(),
                artist.getFollowerCount(),
                follow.getCreateDate(),
                true
        );
    }
}
