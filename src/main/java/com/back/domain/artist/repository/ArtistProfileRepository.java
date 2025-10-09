package com.back.domain.artist.repository;

import com.back.domain.artist.entity.ArtistProfile;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, Long> {

    // User ID로 작가 프로필 조회
    Optional<ArtistProfile> findByUserId(Long artistId);

    // User ID로 작가 프로필 존재 여부 확인
    boolean existsByUserId(Long userId);

    // 작가명으로 작가 프로필 조회
    Optional<ArtistProfile> findByArtistName(String artistName);

    // 작가명으로 작가 프로필 존재 여부 확인
    boolean existsByArtistName(String artistName);

    // User 엔티티를 함께 페치하여 작가 프로필 조회
    @Query("SELECT ap FROM ArtistProfile ap JOIN FETCH ap.user WHERE ap.user.id = :userId")
    Optional<ArtistProfile> findByUserIdWithUser(@Param("userId") Long userId);

    // 모든 작가 목록 조회 (이름순)
    List<ArtistProfile> findAllByOrderByArtistNameAsc();

}
