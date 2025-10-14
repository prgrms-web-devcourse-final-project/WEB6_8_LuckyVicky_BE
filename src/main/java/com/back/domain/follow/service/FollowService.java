package com.back.domain.follow.service;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.follow.dto.response.FollowResponse;
import com.back.domain.follow.dto.response.FollowerListResponse;
import com.back.domain.follow.dto.response.FollowingListResponse;
import com.back.domain.follow.entity.Follow;
import com.back.domain.follow.repository.FollowRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ArtistProfileRepository artistProfileRepository;

    /**
     * 작가 팔로우
     */
    @Transactional
    public FollowResponse followArtist(Long userId, Long artistId) {
        User user = getUserById(userId);
        ArtistProfile artist = getArtistById(artistId);

        validateNotAlreadyFollowing(userId, artistId);

        Follow follow = Follow.create(user, artist);
        followRepository.save(follow);

        artist.increaseFollowerCount();

        log.info("작가 팔로우 완료 - userId: {}, artistId: {}", userId, artistId);

        return FollowResponse.from(follow, artist);
    }

    /**
     * 작가 언팔로우
     */
    @Transactional
    public void unfollowArtist(Long userId, Long artistId) {
        Follow follow = getFollowByUserAndArtist(userId, artistId);
        follow.validateFollower(userId);

        ArtistProfile artist = getArtistById(artistId);
        artist.decreaseFollowerCount();

        followRepository.delete(follow);

        log.info("작가 언팔로우 완료 - userId: {}, artistId: {}", userId, artistId);
    }

    /**
     * 팔로우 여부 확인
     */
    public boolean isFollowing(Long userId, Long artistId) {
        return followRepository.existsByFollowerIdAndFollowingArtistId(userId, artistId);
    }

    /**
     * 사용자의 팔로잉 목록 조회
     */
    public List<FollowingListResponse> getMyFollowingList(Long userId) {
        validateUserExists(userId);

        List<Follow> follows = followRepository.findFollowingsByFollowerId(userId);

        return follows.stream()
                .map(FollowingListResponse::from)
                .toList();
    }

    /**
     * 작가의 팔로워 목록 조회 (작가 본인만 조회 가능)
     */
    public List<FollowerListResponse> getMyFollowerList(Long userId) {
        ArtistProfile artist = artistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ServiceException("404", "작가 본인만 팔로워 목록을 조회할 수 있습니다."));

        List<Follow> follows = followRepository.findFollowersByArtistId(artist.getId());

        return follows.stream()
                .map(FollowerListResponse::from)
                .toList();
    }

    /**
     * 사용자가 팔로우하는 작가 수 조회
     */
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    /**
     * 작가의 팔로워 수 조회
     */
    public long getFollowerCount(Long artistId) {
        return followRepository.countByFollowingArtistId(artistId);
    }

    // ===== 헬퍼 메서드 ===== //

    /**
     * 사용자 조회
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));
    }

    /**
     * 작가 프로필 조회
     */
    private ArtistProfile getArtistById(Long artistId) {
        return artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new ServiceException("404", "작가를 찾을 수 없습니다."));
    }

    /**
     * 팔로우 관계 조회
     */
    private Follow getFollowByUserAndArtist(Long userId, Long artistId) {
        return followRepository.findByFollowerIdAndFollowingArtistId(userId, artistId)
                .orElseThrow(() -> new ServiceException("404", "팔로우 관계를 찾을 수 없습니다."));
    }

    /**
     * 사용자 존재 여부 확인
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.");
        }
    }

    /**
     * 이미 팔로우 중인지 확인
     */
    private void validateNotAlreadyFollowing(Long userId, Long artistId) {
        if (followRepository.existsByFollowerIdAndFollowingArtistId(userId, artistId)) {
            throw new ServiceException("400", "이미 팔로우 중입니다.");
        }
    }

    /**
     * 작가 본인 확인
     */
    private void validateArtistOwnership(ArtistProfile artist, Long requestUserId) {
        if (!artist.isOwnedBy(requestUserId)) {
            throw new ServiceException("403", "본인의 팔로워 목록만 조회할 수 있습니다.");
        }
    }

}
