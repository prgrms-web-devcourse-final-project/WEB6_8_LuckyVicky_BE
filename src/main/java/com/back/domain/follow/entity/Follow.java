package com.back.domain.follow.entity;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔로우 엔티티
 */
@Getter
@Entity
@Table(
        name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follower_artist",
                        columnNames = {"follower_id", "following_artist_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_artist_id", nullable = false)
    private ArtistProfile followingArtist;

    @Builder
    public Follow(User follower, ArtistProfile followingArtist) {
        this.follower = follower;
        this.followingArtist = followingArtist;
    }

    // ===== 정적 팩토리 메서드 ===== //

    /**
     * 팔로우 관계 생성
     */
    public static Follow create(User follower, ArtistProfile artist) {
        validateFollowCreation(follower, artist);

        return Follow.builder()
                .follower(follower)
                .followingArtist(artist)
                .build();
    }

    // ===== 검증 메서드 ===== //

    /**
     * 팔로우 생성 유효성 검증
     */
    private static void validateFollowCreation(User follower, ArtistProfile artist) {
        if (follower == null) {
            throw new ServiceException("400", "팔로우하는 사용자 정보가 없습니다.");
        }

        if (artist == null) {
            throw new ServiceException("400", "팔로우 대상 작가 정보가 없습니다.");
        }

        // 자기 자신을 팔로우하는 것 방지
        if (follower.getId().equals(artist.getUser().getId())) {
            throw new ServiceException("400", "자기 자신을 팔로우할 수 없습니다.");
        }
    }

    /**
     * 팔로우한 사용자 본인인지 확인
     */
    public boolean isFollowedBy(Long userId) {
        return this.follower.getId().equals(userId);
    }

    /**
     * 팔로우한 사용자가 아닌 경우 예외 발생
     */
    public void validateFollower(Long userId) {
        if (!isFollowedBy(userId)) {
            throw new ServiceException("403", "본인이 팔로우한 관계만 삭제할 수 있습니다.");
        }
    }

    /**
     * 팔로우 시작 시간 조회 (BaseEntity의 createDate 사용)
     */
    public LocalDateTime getFollowedAt() {
        return this.getCreateDate();
    }
}
