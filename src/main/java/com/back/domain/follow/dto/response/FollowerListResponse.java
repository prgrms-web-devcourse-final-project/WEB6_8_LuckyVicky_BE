package com.back.domain.follow.dto.response;

import com.back.domain.follow.entity.Follow;
import com.back.domain.user.entity.User;

import java.time.LocalDateTime;

public record FollowerListResponse(
        Long followId,
        Long userId,
        String userName,
        String profileImageUrl,
        String grade,
        LocalDateTime followedAt
) {
    /**
     * Follow 엔티티 -> DTO 변환
     */
    public static FollowerListResponse from(Follow follow) {
        User follower = follow.getFollower();

        return new FollowerListResponse(
                follow.getId(),
                follower.getId(),
                follower.getName(),
                follower.getProfileImageUrl(),
                follower.getGrade().name(),
                follow.getCreateDate()
        );
    }
}