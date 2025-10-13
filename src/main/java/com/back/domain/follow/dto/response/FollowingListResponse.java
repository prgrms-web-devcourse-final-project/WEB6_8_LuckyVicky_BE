package com.back.domain.follow.dto.response;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.follow.entity.Follow;

import java.time.LocalDateTime;

/**
 * 팔로잉 목록 응답 DTO
 */
public record FollowingListResponse(
        Long followId,
        Long artistId,
        String artistName,
        String profileImageUrl,
        Integer followerCount,
        LocalDateTime followedAt
) {
    /**
     * Follow 엔티티 -> DTO 변환
     */
    public static FollowingListResponse from(Follow follow) {
        ArtistProfile artist = follow.getFollowingArtist();

        return new FollowingListResponse(
                follow.getId(),
                artist.getId(),
                artist.getArtistName(),
                artist.getProfileImageUrl(),
                artist.getFollowerCount(),
                follow.getCreateDate()
        );
    }
}