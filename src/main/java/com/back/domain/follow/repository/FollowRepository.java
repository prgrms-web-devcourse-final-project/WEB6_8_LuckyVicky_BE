package com.back.domain.follow.repository;

import com.back.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {


    boolean existsByFollowerIdAndFollowingArtistId(Long followerId, Long artistId);
    Optional<Follow> findByFollowerIdAndFollowingArtistId(Long followerId, Long artistId);
    long countByFollowingArtistId(Long artistId);
    long countByFollowerId(Long followerId);

    // 팔로우 목록 조회 (사용자용)
    @Query("SELECT f FROM Follow f " +
            "JOIN FETCH f.followingArtist a " +
            "JOIN FETCH a.user " +
            "WHERE f.follower.id = :followerId " +
            "ORDER BY f.createDate DESC")
    List<Follow> findFollowingsByFollowerId(@Param("followerId") Long followerId);

    // 나를 팔로우하는 사용자 목록 조회 (작가용)
    @Query("SELECT f FROM Follow f " +
            "JOIN FETCH f.follower " +
            "WHERE f.followingArtist.id = :artistId " +
            "ORDER BY f.createDate DESC")
    List<Follow> findFollowersByArtistId(@Param("artistId") Long artistId);

    // 삭제 관련 (회원 및 작가가 탈퇴 시 사용)
    void deleteByFollowerId(Long followerId);
    void deleteByFollowingArtistId(Long artistId);
}
